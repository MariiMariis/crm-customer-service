package com.pb.crm.sales.api;

import com.pb.crm.commons.messaging.EventEnvelope;
import com.pb.crm.sales.application.lead.LeadService;
import com.pb.crm.sales.domain.reference.ReferenceRepository;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import com.pb.crm.sales.domain.reference.SalesRepRefRepository;
import com.pb.crm.sales.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LeadConversionSagaIntegrationTest extends IntegrationTestSupport {

    private static final long OWNER_ID = 6001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SalesRepRefRepository salesRepRefRepository;

    @Autowired
    private ReferenceRepository referenceRepository;

    @Autowired
    private LeadService leadService;

    private String requests;

    @BeforeEach
    void setUp() {
        salesRepRefRepository.upsert(new SalesRepRef(OWNER_ID, "Paulo Nogueira", "paulo@pbtech.com.br", null, true, false));
        requests = bindProbe("sales.events", "sales.lead.conversion-requested");
    }

    private long qualifiedLead(String email) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Camila");
        body.put("lastName", "Rocha");
        body.put("email", email);
        body.put("phone", "11955554444");
        body.put("companyName", "Rede Farma Bem");
        body.put("jobTitle", "CIO");
        body.put("source", "EVENT");
        body.put("estimatedValue", 320000);
        body.put("ownerId", OWNER_ID);
        String response = mockMvc.perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON)
                        .content(testObjectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = testObjectMapper.readTree(response).get("id").asLong();
        mockMvc.perform(post("/api/leads/{id}/contacted", id)).andExpect(status().isOk());
        mockMvc.perform(post("/api/leads/{id}/qualify", id)).andExpect(status().isOk());
        return id;
    }

    private void convert(long id) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("cnpj", "11.222.333/0001-81");
        body.put("industry", "HEALTHCARE");
        body.put("companySize", "LARGE");
        body.put("state", "SP");
        body.put("createOpportunity", true);
        body.put("opportunityTitle", "Rede de lojas com Wi-Fi gerenciado");
        body.put("expectedCloseDate", LocalDate.now().plusDays(30).toString());
        mockMvc.perform(post("/api/leads/{id}/convert", id).contentType(MediaType.APPLICATION_JSON)
                        .content(testObjectMapper.writeValueAsString(body)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("CONVERTING"));
    }

    private static String uniqueEmail() {
        return "camila." + UUID.randomUUID().toString().substring(0, 8) + "@farmabem.com.br";
    }

    private void reply(String type, long leadId, Map<String, Object> payload) {
        publish("accounts.events", envelope(type, "Lead", leadId, payload, Instant.now()));
    }

    private static Map<String, Object> provisioned(long leadId, long companyId, long contactId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadId);
        payload.put("companyId", companyId);
        payload.put("companyName", "Rede Farma Bem");
        payload.put("cnpj", "11222333000181");
        payload.put("companyOwnerId", OWNER_ID);
        payload.put("companyCreated", true);
        payload.put("contactId", contactId);
        payload.put("contactName", "Camila Rocha");
        payload.put("contactEmail", "camila@farmabem.com.br");
        payload.put("contactCreated", true);
        return payload;
    }

    private String statusOf(long id) throws Exception {
        String body = mockMvc.perform(get("/api/leads/{id}", id)).andReturn().getResponse().getContentAsString();
        return testObjectMapper.readTree(body).get("status").asText();
    }

    @Test
    void conversionPublishesRequestAndCompletesWithOpportunityWhenAccountIsProvisioned() throws Exception {
        String email = uniqueEmail();
        long id = qualifiedLead(email);
        convert(id);

        List<EventEnvelope> received = new ArrayList<>();
        await().atMost(Duration.ofSeconds(15)).until(() -> {
            receive(requests).ifPresent(received::add);
            return received.stream().anyMatch(e -> e.aggregateId().equals(String.valueOf(id)));
        });
        EventEnvelope request = received.stream().filter(e -> e.aggregateId().equals(String.valueOf(id))).findFirst().orElseThrow();
        assertThat(request.payload().get("email").asText()).isEqualTo(email);
        assertThat(request.payload().get("cnpj").asText()).isEqualTo("11.222.333/0001-81");
        assertThat(request.payload().get("ownerId").asLong()).isEqualTo(OWNER_ID);

        reply("accounts.lead-account.provisioned", id, provisioned(id, 7001L, 7101L));

        await().atMost(Duration.ofSeconds(15)).until(() -> statusOf(id).equals("CONVERTED"));
        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(jsonPath("$.convertedCompanyId").value(7001))
                .andExpect(jsonPath("$.convertedContactId").value(7101))
                .andExpect(jsonPath("$.convertedOpportunityId").isNumber());
        assertThat(referenceRepository.findCompany(7001L)).isPresent();

        String lead = mockMvc.perform(get("/api/leads/{id}", id)).andReturn().getResponse().getContentAsString();
        long opportunityId = testObjectMapper.readTree(lead).get("convertedOpportunityId").asLong();
        mockMvc.perform(get("/api/opportunities/{id}", opportunityId))
                .andExpect(jsonPath("$.companyName").value("Rede Farma Bem"))
                .andExpect(jsonPath("$.contactName").value("Camila Rocha"))
                .andExpect(jsonPath("$.leadId").value(id))
                .andExpect(jsonPath("$.amount").value(320000.0));
    }

    @Test
    void rejectionCompensatesTheLeadBackToQualified() throws Exception {
        long id = qualifiedLead(uniqueEmail());
        convert(id);

        reply("accounts.lead-account.rejected", id, Map.of("leadId", id, "reason", "CNPJ invalido: digitos verificadores nao conferem"));

        await().atMost(Duration.ofSeconds(15)).until(() -> statusOf(id).equals("QUALIFIED"));
        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(jsonPath("$.conversionFailureReason").value("CNPJ invalido: digitos verificadores nao conferem"));
    }

    @Test
    void timeoutCompensatesAndLateReplyIsIgnored() throws Exception {
        long id = qualifiedLead(uniqueEmail());
        convert(id);

        int expired = leadService.expireStaleConversions(Instant.now().plusSeconds(1));
        assertThat(expired).isGreaterThanOrEqualTo(1);
        assertThat(statusOf(id)).isEqualTo("QUALIFIED");

        reply("accounts.lead-account.provisioned", id, provisioned(id, 7002L, 7102L));
        await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3)).until(() -> true);

        assertThat(statusOf(id)).isEqualTo("QUALIFIED");
        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(jsonPath("$.conversionFailureReason").value("tempo esgotado aguardando a resposta do servico de contas"));
    }

    @Test
    void leadWithoutEmailCannotBeConverted() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Sem");
        body.put("lastName", "Email");
        body.put("phone", "11911112222");
        body.put("companyName", "Empresa Sem Email");
        body.put("source", "COLD_CALL");
        body.put("ownerId", OWNER_ID);
        String response = mockMvc.perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON)
                        .content(testObjectMapper.writeValueAsString(body)))
                .andReturn().getResponse().getContentAsString();
        long id = testObjectMapper.readTree(response).get("id").asLong();
        mockMvc.perform(post("/api/leads/{id}/contacted", id));
        mockMvc.perform(post("/api/leads/{id}/qualify", id));

        Map<String, Object> conversion = new HashMap<>();
        conversion.put("cnpj", "11.222.333/0001-81");
        conversion.put("industry", "RETAIL");
        conversion.put("companySize", "SMALL");
        conversion.put("createOpportunity", false);
        mockMvc.perform(post("/api/leads/{id}/convert", id).contentType(MediaType.APPLICATION_JSON)
                        .content(testObjectMapper.writeValueAsString(conversion)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("informe o e-mail do lead antes de converter; ele sera o contato da empresa"));
    }

    @Test
    void companyAndContactEventsFeedTheReplicas() {
        Instant now = Instant.now();
        publish("accounts.events", envelope("accounts.company.created", "Company", 7200L,
                Map.of("id", 7200L, "displayName", "Metalurgica Sul", "cnpj", "11222333000181", "ownerId", OWNER_ID,
                        "archived", false), now));
        publish("accounts.events", envelope("accounts.contact.created", "Contact", 7300L,
                Map.of("id", 7300L, "companyId", 7200L, "fullName", "Bruno Dias", "email", "bruno@metsul.com.br",
                        "active", true, "archived", false), now));

        await().atMost(Duration.ofSeconds(15)).until(() -> referenceRepository.findCompany(7200L).isPresent()
                && referenceRepository.findContact(7300L).isPresent());
        assertThat(referenceRepository.findContact(7300L).orElseThrow().companyId()).isEqualTo(7200L);
    }
}
