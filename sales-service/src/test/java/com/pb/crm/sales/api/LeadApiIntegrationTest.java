package com.pb.crm.sales.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pb.crm.sales.application.lead.LeadService;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import com.pb.crm.sales.domain.reference.SalesRepRefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class LeadApiIntegrationTest {

    private static final long ANA_ID = 701L;
    private static final long INACTIVE_ID = 702L;

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SalesRepRefRepository salesRepRefRepository;

    @Autowired
    private LeadService leadService;

    @BeforeEach
    void syncSalesReps() {
        salesRepRefRepository.upsert(new SalesRepRef(ANA_ID, "Ana Ribeiro", "ana@pbtech.com.br", null, true, false));
        salesRepRefRepository.upsert(new SalesRepRef(INACTIVE_ID, "Caio Prado", "caio@pbtech.com.br", null, false, false));
    }

    private ResultActions send(MockHttpServletRequestBuilder builder, Object body) throws Exception {
        return mockMvc.perform(builder
                .header("X-Actor", "sdr.julia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private static Map<String, Object> leadPayload(String email, Long ownerId) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Marcos");
        body.put("lastName", "Teixeira");
        body.put("email", email);
        body.put("phone", "11912345678");
        body.put("companyName", "Grupo Varejo Mais");
        body.put("jobTitle", "Diretor de TI");
        body.put("source", "WEBSITE");
        body.put("estimatedValue", 180000);
        body.put("ownerId", ownerId);
        return body;
    }

    private static String uniqueEmail() {
        return "marcos." + UUID.randomUUID().toString().substring(0, 8) + "@varejomais.com.br";
    }

    private JsonNode createLead(Long ownerId) throws Exception {
        String body = send(post("/api/leads"), leadPayload(uniqueEmail(), ownerId))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private static Map<String, Object> conversionPayload() {
        Map<String, Object> body = new HashMap<>();
        body.put("cnpj", "11.222.333/0001-81");
        body.put("industry", "RETAIL");
        body.put("companySize", "LARGE");
        body.put("city", "Sao Paulo");
        body.put("state", "SP");
        body.put("createOpportunity", true);
        body.put("opportunityTitle", "Modernizacao de PDVs");
        body.put("expectedCloseDate", LocalDate.now().plusDays(45).toString());
        return body;
    }

    @Test
    void createsLeadWithScoreBreakdownAndOptionalOwner() throws Exception {
        JsonNode lead = createLead(null);

        mockMvc.perform(get("/api/leads/{id}", lead.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.score").value(10 + 10 + 20 + 10 + 15))
                .andExpect(jsonPath("$.scoreFactors.length()").value(5))
                .andExpect(jsonPath("$.ownerId").doesNotExist())
                .andExpect(jsonPath("$.createdBy").value("sdr.julia"));

        mockMvc.perform(get("/api/leads").param("unassigned", "true").param("pageSize", "100"))
                .andExpect(jsonPath("$.content[*].id").value(hasItem(lead.get("id").intValue())));
    }

    @Test
    void rejectsDuplicateOpenEmailButAllowsItAfterDisqualification() throws Exception {
        String email = uniqueEmail();
        JsonNode lead = objectMapper.readTree(send(post("/api/leads"), leadPayload(email, null))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());

        send(post("/api/leads"), leadPayload(email.toUpperCase(), null))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("ja existe um lead aberto com este email"));

        send(post("/api/leads/{id}/disqualify", lead.get("id").asLong()), Map.of("reason", "sem fit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNQUALIFIED"));

        send(post("/api/leads"), leadPayload(email, null)).andExpect(status().isCreated());

        mockMvc.perform(post("/api/leads/{id}/reopen", lead.get("id").asLong()))
                .andExpect(status().isConflict());
    }

    @Test
    void assignmentRequiresSyncedActiveSalesRep() throws Exception {
        long id = createLead(null).get("id").asLong();

        send(post("/api/leads/{id}/assign", id), Map.of("ownerId", 999)).andExpect(status().isConflict());
        send(post("/api/leads/{id}/assign", id), Map.of("ownerId", INACTIVE_ID)).andExpect(status().isConflict());
        send(post("/api/leads/{id}/assign", id), Map.of("ownerId", ANA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName").value("Ana Ribeiro"));
    }

    @Test
    void walksTheFunnelAndRequestsConversionAsynchronously() throws Exception {
        long id = createLead(ANA_ID).get("id").asLong();

        send(post("/api/leads/{id}/convert", id), conversionPayload()).andExpect(status().isConflict());

        mockMvc.perform(post("/api/leads/{id}/contacted", id)).andExpect(jsonPath("$.status").value("CONTACTED"));
        mockMvc.perform(post("/api/leads/{id}/qualify", id))
                .andExpect(jsonPath("$.status").value("QUALIFIED"))
                .andExpect(jsonPath("$.score").value(80));

        send(post("/api/leads/{id}/convert", id), conversionPayload())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("CONVERTING"))
                .andExpect(jsonPath("$.conversion.industry").value("RETAIL"));

        send(put("/api/leads/{id}", id), leadPayload(uniqueEmail(), ANA_ID)).andExpect(status().isConflict());
        mockMvc.perform(post("/api/leads/{id}/archive", id)).andExpect(status().isConflict());

        leadService.failConversion(id, "CNPJ ja pertence a um cliente de outro vendedor");
        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(jsonPath("$.status").value("QUALIFIED"))
                .andExpect(jsonPath("$.conversionFailureReason").value("CNPJ ja pertence a um cliente de outro vendedor"));

        send(post("/api/leads/{id}/convert", id), conversionPayload()).andExpect(status().isAccepted());
        leadService.completeConversion(id, 11L, 22L, null);
        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(jsonPath("$.status").value("CONVERTED"))
                .andExpect(jsonPath("$.convertedCompanyId").value(11))
                .andExpect(jsonPath("$.convertedContactId").value(22));
    }

    @Test
    void conversionPayloadIsValidatedAndStatsAreAggregated() throws Exception {
        long id = createLead(ANA_ID).get("id").asLong();
        mockMvc.perform(post("/api/leads/{id}/contacted", id));
        mockMvc.perform(post("/api/leads/{id}/qualify", id));

        Map<String, Object> missingTitle = conversionPayload();
        missingTitle.remove("opportunityTitle");
        send(post("/api/leads/{id}/convert", id), missingTitle).andExpect(status().isBadRequest());

        Map<String, Object> pastDate = conversionPayload();
        pastDate.put("expectedCloseDate", LocalDate.now().minusDays(1).toString());
        send(post("/api/leads/{id}/convert", id), pastDate).andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/leads/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byStatus.QUALIFIED").value(greaterThan(0)))
                .andExpect(jsonPath("$.open").value(greaterThan(0)));

        mockMvc.perform(get("/api/leads/{id}/revisions", id))
                .andExpect(jsonPath("$[0].type").value("INSERT"))
                .andExpect(jsonPath("$[2].data.status").value("QUALIFIED"));
    }
}
