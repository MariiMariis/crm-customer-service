package com.pb.crm.sales.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pb.crm.sales.domain.opportunity.BillingType;
import com.pb.crm.sales.domain.reference.CompanyRef;
import com.pb.crm.sales.domain.reference.ContactRef;
import com.pb.crm.sales.domain.reference.ProductRef;
import com.pb.crm.sales.domain.reference.ReferenceRepository;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import com.pb.crm.sales.domain.reference.SalesRepRefRepository;
import com.pb.crm.sales.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpportunityApiIntegrationTest extends IntegrationTestSupport {

    private static final long MANAGER_ID = 800L;
    private static final long OWNER_ID = 801L;
    private static final long COMPANY_ID = 900L;
    private static final long OTHER_COMPANY_ID = 901L;
    private static final long CONTACT_ID = 950L;
    private static final long FOREIGN_CONTACT_ID = 951L;
    private static final long NOTEBOOK_ID = 1000L;
    private static final long SUBSCRIPTION_ID = 1001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SalesRepRefRepository salesRepRefRepository;

    @Autowired
    private ReferenceRepository referenceRepository;

    @BeforeEach
    void syncReferences() {
        salesRepRefRepository.upsert(new SalesRepRef(MANAGER_ID, "Marina Costa", "marina@pbtech.com.br", null, true, false));
        salesRepRefRepository.upsert(new SalesRepRef(OWNER_ID, "Joao Lima", "joao@pbtech.com.br", MANAGER_ID, true, false));
        referenceRepository.upsertCompany(new CompanyRef(COMPANY_ID, "Santa Clara", "11222333000181", OWNER_ID, false));
        referenceRepository.upsertCompany(new CompanyRef(OTHER_COMPANY_ID, "LogiSul", null, OWNER_ID, false));
        referenceRepository.upsertContact(new ContactRef(CONTACT_ID, COMPANY_ID, "Paula Silva", "paula@santaclara.org.br", true, false));
        referenceRepository.upsertContact(new ContactRef(FOREIGN_CONTACT_ID, OTHER_COMPANY_ID, "Rodrigo Nunes", null, true, false));
        referenceRepository.upsertProduct(new ProductRef(NOTEBOOK_ID, "HW-NBK-000001", "Notebook Dell Latitude 5450",
                "HARDWARE", BillingType.ONE_TIME, new BigDecimal("8500.00"), new BigDecimal("8.00"), true, false));
        referenceRepository.upsertProduct(new ProductRef(SUBSCRIPTION_ID, "SW-SEC-000002", "Antivirus corporativo",
                "SOFTWARE", BillingType.MONTHLY, new BigDecimal("25.00"), new BigDecimal("10.00"), true, false));
    }

    private ResultActions send(MockHttpServletRequestBuilder builder, Object body) throws Exception {
        return mockMvc.perform(builder
                .header("X-Actor", "joao.lima")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private static Map<String, Object> opportunityPayload(Long contactId) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Renovacao de notebooks e seguranca");
        body.put("companyId", COMPANY_ID);
        body.put("contactId", contactId);
        body.put("ownerId", OWNER_ID);
        body.put("expectedCloseDate", LocalDate.now().plusDays(40).toString());
        body.put("contractTermMonths", 24);
        return body;
    }

    private JsonNode createOpportunity() throws Exception {
        return objectMapper.readTree(send(post("/api/opportunities"), opportunityPayload(CONTACT_ID))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private JsonNode addItem(long id, long productId, int quantity, double discount) throws Exception {
        return objectMapper.readTree(send(post("/api/opportunities/{id}/items", id),
                Map.of("productId", productId, "quantity", quantity, "discountPercent", discount))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    @Test
    void createsOpportunityWithNamesFromReplicasAndInitialHistory() throws Exception {
        JsonNode opportunity = createOpportunity();

        mockMvc.perform(get("/api/opportunities/{id}", opportunity.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Santa Clara"))
                .andExpect(jsonPath("$.contactName").value("Paula Silva"))
                .andExpect(jsonPath("$.ownerName").value("Joao Lima"))
                .andExpect(jsonPath("$.stage").value("PROSPECTING"))
                .andExpect(jsonPath("$.probability").value(10))
                .andExpect(jsonPath("$.stageHistory[0].changedBy").value("joao.lima"));
    }

    @Test
    void rejectsUnknownCompanyAndContactFromAnotherCompany() throws Exception {
        Map<String, Object> unknownCompany = opportunityPayload(null);
        unknownCompany.put("companyId", 123456);
        send(post("/api/opportunities"), unknownCompany).andExpect(status().isConflict());

        send(post("/api/opportunities"), opportunityPayload(FOREIGN_CONTACT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("o contato informado nao pertence a empresa da oportunidade"));
    }

    @Test
    void itemsComputeOneTimeRecurringAndContractValue() throws Exception {
        long id = createOpportunity().get("id").asLong();
        addItem(id, NOTEBOOK_ID, 10, 5);
        JsonNode result = addItem(id, SUBSCRIPTION_ID, 200, 0);

        assertThat(result.get("oneTimeValue").decimalValue()).isEqualByComparingTo("80750.00");
        assertThat(result.get("monthlyRecurringValue").decimalValue()).isEqualByComparingTo("5000.00");
        assertThat(result.get("amount").decimalValue()).isEqualByComparingTo("200750.00");

        long itemId = result.get("items").get(1).get("id").asLong();
        send(put("/api/opportunities/{id}/items/{itemId}", id, itemId), Map.of("productId", SUBSCRIPTION_ID, "quantity", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyRecurringValue").value(2500.00));

        mockMvc.perform(delete("/api/opportunities/{id}/items/{itemId}", id, itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void discountAboveLimitIsApprovedOnlyByOwnersManagerBeforeWinning() throws Exception {
        long id = createOpportunity().get("id").asLong();
        JsonNode withDiscount = addItem(id, NOTEBOOK_ID, 50, 15);
        assertThat(withDiscount.get("discountApproval").asText()).isEqualTo("PENDING");
        assertThat(withDiscount.get("items").get(0).get("aboveDiscountLimit").asBoolean()).isTrue();

        mockMvc.perform(post("/api/opportunities/{id}/win", id)).andExpect(status().isConflict());

        send(post("/api/opportunities/{id}/discount-decision", id), Map.of("approverId", OWNER_ID, "approved", true))
                .andExpect(status().isConflict());
        send(post("/api/opportunities/{id}/discount-decision", id),
                Map.of("approverId", MANAGER_ID, "approved", true, "comment", "volume estrategico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountApproval").value("APPROVED"));

        mockMvc.perform(post("/api/opportunities/{id}/win", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stage").value("WON"))
                .andExpect(jsonPath("$.open").value(false));
    }

    @Test
    void kanbanMovesProbabilityLossReopenAndPipeline() throws Exception {
        long id = createOpportunity().get("id").asLong();
        addItem(id, NOTEBOOK_ID, 4, 0);

        send(post("/api/opportunities/{id}/stage", id), Map.of("stage", "NEGOTIATION"))
                .andExpect(jsonPath("$.probability").value(75));
        send(post("/api/opportunities/{id}/probability", id), Map.of("probability", 90))
                .andExpect(jsonPath("$.probability").value(90));
        send(post("/api/opportunities/{id}/stage", id), Map.of("stage", "WON")).andExpect(status().isConflict());

        send(post("/api/opportunities/{id}/lose", id), Map.of("reason", "cliente adiou o projeto"))
                .andExpect(jsonPath("$.stage").value("LOST"))
                .andExpect(jsonPath("$.lossReason").value("cliente adiou o projeto"));

        mockMvc.perform(post("/api/opportunities/{id}/reopen", id))
                .andExpect(jsonPath("$.stage").value("NEGOTIATION"))
                .andExpect(jsonPath("$.stageHistory.length()").value(4));

        mockMvc.perform(get("/api/opportunities/pipeline").param("ownerId", String.valueOf(OWNER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stages.length()").value(6))
                .andExpect(jsonPath("$.openCount").value(greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/opportunities").param("stage", "NEGOTIATION").param("companyId", String.valueOf(COMPANY_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].companyName").value("Santa Clara"));

        mockMvc.perform(get("/api/opportunities/{id}/revisions", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("INSERT"));
    }
}
