package com.pb.crm.accounts.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pb.crm.accounts.domain.company.Cnpj;
import com.pb.crm.accounts.domain.salesrep.SalesRepRef;
import com.pb.crm.accounts.domain.salesrep.SalesRepRefRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountsApiIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SalesRepRefRepository salesRepRefRepository;

    private static String randomCnpj() {
        long base = ThreadLocalRandom.current().nextLong(100_000_000_000L, 999_999_999_999L);
        return Cnpj.fromBase(Long.toString(base)).formatted();
    }

    private static String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID().toString().substring(0, 8) + "@cliente.com.br";
    }

    private ResultActions send(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
                               Object body) throws Exception {
        return mockMvc.perform(builder
                .header("X-Actor", "vendedora.ana")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private Map<String, Object> companyPayload(String cnpj, Long ownerId) {
        Map<String, Object> body = new HashMap<>();
        body.put("legalName", "Hospital Santa Clara Ltda");
        body.put("tradeName", "Santa Clara");
        body.put("cnpj", cnpj);
        body.put("industry", "HEALTHCARE");
        body.put("size", "LARGE");
        body.put("employees", 1200);
        body.put("annualRevenue", 350000000);
        body.put("website", "santaclara.org.br");
        body.put("city", "Belo Horizonte");
        body.put("state", "MG");
        body.put("type", "PROSPECT");
        body.put("ownerId", ownerId);
        return body;
    }

    private JsonNode createCompany(Long ownerId) throws Exception {
        String body = send(post("/api/companies"), companyPayload(randomCnpj(), ownerId))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private Map<String, Object> contactPayload(long companyId, String firstName, Boolean primary) {
        Map<String, Object> body = new HashMap<>();
        body.put("companyId", companyId);
        body.put("firstName", firstName);
        body.put("lastName", "Silva");
        body.put("email", uniqueEmail(firstName.toLowerCase()));
        body.put("jobTitle", "Gerente de TI");
        body.put("decisionRole", "DECISION_MAKER");
        body.put("primary", primary);
        return body;
    }

    private JsonNode createContact(long companyId, String firstName, Boolean primary) throws Exception {
        String body = send(post("/api/contacts"), contactPayload(companyId, firstName, primary))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    @Test
    void createsCompanyWithSyncedOwnerAndFormatsCnpj() throws Exception {
        salesRepRefRepository.upsert(new SalesRepRef(501L, "Ana Ribeiro", "ana@pbtech.com.br", true, false));

        JsonNode company = createCompany(501L);

        mockMvc.perform(get("/api/companies/{id}", company.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName").value("Ana Ribeiro"))
                .andExpect(jsonPath("$.displayName").value("Santa Clara"))
                .andExpect(jsonPath("$.website").value("https://santaclara.org.br"))
                .andExpect(jsonPath("$.cnpj").value(company.get("cnpj").asText()))
                .andExpect(jsonPath("$.contactCount").value(0))
                .andExpect(jsonPath("$.createdBy").value("vendedora.ana"));
    }

    @Test
    void rejectsInvalidCnpjDuplicateCnpjAndUnknownOrInactiveOwner() throws Exception {
        send(post("/api/companies"), companyPayload("11.222.333/0001-99", null))
                .andExpect(status().isBadRequest());

        String cnpj = randomCnpj();
        send(post("/api/companies"), companyPayload(cnpj, null)).andExpect(status().isCreated());
        send(post("/api/companies"), companyPayload(cnpj, null)).andExpect(status().isConflict());

        send(post("/api/companies"), companyPayload(randomCnpj(), 999L))
                .andExpect(status().isConflict());

        salesRepRefRepository.upsert(new SalesRepRef(502L, "Bruno Alves", "bruno@pbtech.com.br", false, false));
        send(post("/api/companies"), companyPayload(randomCnpj(), 502L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("o vendedor responsavel informado esta inativo ou arquivado"));
    }

    @Test
    void firstContactBecomesPrimaryAndPromotingAnotherReleasesThePrevious() throws Exception {
        long companyId = createCompany(null).get("id").asLong();

        JsonNode first = createContact(companyId, "Paula", null);
        JsonNode second = createContact(companyId, "Rafael", null);

        mockMvc.perform(get("/api/contacts/{id}", first.get("id").asLong()))
                .andExpect(jsonPath("$.primary").value(true))
                .andExpect(jsonPath("$.companyName").value("Santa Clara"));
        mockMvc.perform(get("/api/contacts/{id}", second.get("id").asLong()))
                .andExpect(jsonPath("$.primary").value(false));

        mockMvc.perform(post("/api/contacts/{id}/make-primary", second.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primary").value(true));
        mockMvc.perform(get("/api/contacts/{id}", first.get("id").asLong()))
                .andExpect(jsonPath("$.primary").value(false));

        JsonNode third = createContact(companyId, "Simone", true);
        mockMvc.perform(get("/api/companies/{id}", companyId))
                .andExpect(jsonPath("$.primaryContactId").value(third.get("id").asLong()))
                .andExpect(jsonPath("$.contactCount").value(3));
    }

    @Test
    void contactEmailIsUniqueAndContactCannotMoveToAnotherCompany() throws Exception {
        long companyId = createCompany(null).get("id").asLong();
        long otherCompanyId = createCompany(null).get("id").asLong();
        JsonNode contact = createContact(companyId, "Lucas", null);

        Map<String, Object> duplicate = contactPayload(companyId, "Outro", null);
        duplicate.put("email", contact.get("email").asText());
        send(post("/api/contacts"), duplicate).andExpect(status().isConflict());

        Map<String, Object> move = contactPayload(otherCompanyId, "Lucas", null);
        move.put("email", contact.get("email").asText());
        send(put("/api/contacts/{id}", contact.get("id").asLong()), move)
                .andExpect(status().isConflict());
    }

    @Test
    void archivingCompanyArchivesContactsAndBlocksContactRestoreUntilCompanyIsRestored() throws Exception {
        long companyId = createCompany(null).get("id").asLong();
        long contactId = createContact(companyId, "Helena", null).get("id").asLong();

        mockMvc.perform(post("/api/companies/{id}/archive", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true))
                .andExpect(jsonPath("$.contactCount").value(0));

        mockMvc.perform(get("/api/contacts/{id}", contactId))
                .andExpect(jsonPath("$.archived").value(true))
                .andExpect(jsonPath("$.primary").value(false));

        send(post("/api/contacts"), contactPayload(companyId, "Nova", null))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/contacts/{id}/restore", contactId))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/companies/{id}/restore", companyId)).andExpect(status().isOk());
        mockMvc.perform(post("/api/contacts/{id}/restore", contactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(false));
    }

    @Test
    void searchesCompaniesByTermCnpjAndFiltersAndReturnsRevisions() throws Exception {
        JsonNode company = createCompany(null);
        String digits = company.get("cnpj").asText().replaceAll("\\D", "").substring(0, 8);

        mockMvc.perform(get("/api/companies").param("q", digits))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(company.get("id").asLong()));

        mockMvc.perform(get("/api/companies")
                        .param("industry", "HEALTHCARE")
                        .param("state", "MG")
                        .param("size", "LARGE")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.content[0].industry").value("HEALTHCARE"));

        mockMvc.perform(get("/api/companies/{id}/revisions", company.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("INSERT"))
                .andExpect(jsonPath("$[0].actor").value("vendedora.ana"));
    }
}
