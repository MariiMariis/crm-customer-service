package com.pb.crm.sales.api;

import com.pb.crm.commons.messaging.EventEnvelope;
import com.pb.crm.sales.domain.opportunity.BillingType;
import com.pb.crm.sales.domain.reference.CompanyRef;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SalesBusinessEventsIntegrationTest extends IntegrationTestSupport {

    private static final long MANAGER_ID = 8100L;
    private static final long OWNER_ID = 8101L;
    private static final long COMPANY_ID = 8200L;
    private static final long PRODUCT_ID = 8300L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SalesRepRefRepository salesRepRefRepository;

    @Autowired
    private ReferenceRepository referenceRepository;

    private String probe;
    private final List<EventEnvelope> received = new ArrayList<>();

    @BeforeEach
    void setUp() {
        salesRepRefRepository.upsert(new SalesRepRef(MANAGER_ID, "Gestora Regional", "gestora@pbtech.com.br", null, true, false));
        salesRepRefRepository.upsert(new SalesRepRef(OWNER_ID, "Igor Sales", "igor@pbtech.com.br", MANAGER_ID, true, false));
        referenceRepository.upsertCompany(new CompanyRef(COMPANY_ID, "Transportadora Veloz", null, OWNER_ID, false));
        referenceRepository.upsertProduct(new ProductRef(PRODUCT_ID, "SW-SEC-008300", "Firewall como servico", "SOFTWARE",
                BillingType.MONTHLY, new BigDecimal("1200.00"), new BigDecimal("10.00"), true, false));
        probe = bindProbe("sales.events", "sales.#");
        received.clear();
    }

    private long send(MockHttpServletRequestBuilder builder, Object body) throws Exception {
        String response = mockMvc.perform(builder.header("X-Actor", "igor.sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testObjectMapper.writeValueAsString(body)))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        return testObjectMapper.readTree(response).get("id").asLong();
    }

    private List<EventEnvelope> eventsOf(String aggregateType, long id, int expected) {
        await().atMost(Duration.ofSeconds(15)).until(() -> {
            receive(probe).ifPresent(received::add);
            return matching(aggregateType, id).size() >= expected;
        });
        return matching(aggregateType, id);
    }

    private List<EventEnvelope> matching(String aggregateType, long id) {
        return received.stream()
                .filter(e -> e.aggregateType().equals(aggregateType) && e.aggregateId().equals(String.valueOf(id)))
                .toList();
    }

    @Test
    void leadLifecyclePublishesCreatedAssignedAndQualifiedWithOwnerAndManager() throws Exception {
        Map<String, Object> lead = new HashMap<>();
        lead.put("firstName", "Sofia");
        lead.put("lastName", "Barros");
        lead.put("email", "sofia." + UUID.randomUUID().toString().substring(0, 6) + "@veloz.com.br");
        lead.put("companyName", "Transportadora Veloz");
        lead.put("source", "PARTNER");
        lead.put("ownerId", OWNER_ID);
        long id = send(post("/api/leads"), lead);
        mockMvc.perform(post("/api/leads/{id}/contacted", id));
        mockMvc.perform(post("/api/leads/{id}/qualify", id));

        List<EventEnvelope> events = eventsOf("Lead", id, 3);
        assertThat(events).extracting(EventEnvelope::eventType)
                .containsExactly("sales.lead.created", "sales.lead.assigned", "sales.lead.qualified");
        EventEnvelope assigned = events.get(1);
        assertThat(assigned.payload().get("ownerName").asText()).isEqualTo("Igor Sales");
        assertThat(assigned.payload().get("ownerManagerId").asLong()).isEqualTo(MANAGER_ID);
        assertThat(assigned.actor()).isEqualTo("igor.sales");
    }

    @Test
    void opportunityLifecyclePublishesDiscountFlowStageChangeAndWin() throws Exception {
        Map<String, Object> opportunity = new HashMap<>();
        opportunity.put("title", "Seguranca gerenciada para 12 filiais");
        opportunity.put("companyId", COMPANY_ID);
        opportunity.put("ownerId", OWNER_ID);
        opportunity.put("expectedCloseDate", LocalDate.now().plusDays(20).toString());
        long id = send(post("/api/opportunities"), opportunity);

        send(post("/api/opportunities/{id}/items", id), Map.of("productId", PRODUCT_ID, "quantity", 12, "discountPercent", 15));
        send(post("/api/opportunities/{id}/discount-decision", id), Map.of("approverId", MANAGER_ID, "approved", true));
        send(post("/api/opportunities/{id}/stage", id), Map.of("stage", "NEGOTIATION"));
        send(post("/api/opportunities/{id}/win", id), Map.of());

        List<EventEnvelope> events = eventsOf("Opportunity", id, 5);
        assertThat(events).extracting(EventEnvelope::eventType).containsExactly(
                "sales.opportunity.created",
                "sales.opportunity.discount-approval-requested",
                "sales.opportunity.discount-decided",
                "sales.opportunity.stage-changed",
                "sales.opportunity.won");
        assertThat(events.get(1).payload().get("ownerManagerId").asLong()).isEqualTo(MANAGER_ID);
        assertThat(events.get(3).payload().get("previousStage").asText()).isEqualTo("PROSPECTING");
        EventEnvelope won = events.get(4);
        assertThat(won.payload().get("companyName").asText()).isEqualTo("Transportadora Veloz");
        assertThat(won.payload().get("monthlyRecurringValue").decimalValue()).isEqualByComparingTo("12240.00");
        assertThat(won.payload().get("companyId").asLong()).isEqualTo(COMPANY_ID);
    }

    @Test
    void activityLifecyclePublishesScheduledAndCompleted() throws Exception {
        Map<String, Object> activity = new HashMap<>();
        activity.put("type", "CALL");
        activity.put("subject", "Follow-up da proposta");
        activity.put("relatedType", "COMPANY");
        activity.put("relatedId", COMPANY_ID);
        activity.put("ownerId", OWNER_ID);
        activity.put("dueAt", Instant.now().plus(Duration.ofHours(4)).toString());
        long id = send(post("/api/activities"), activity);
        send(post("/api/activities/{id}/complete", id), Map.of("outcome", "Cliente aprovou o escopo", "durationMinutes", 15));

        List<EventEnvelope> events = eventsOf("Activity", id, 2);
        assertThat(events).extracting(EventEnvelope::eventType)
                .containsExactly("sales.activity.scheduled", "sales.activity.completed");
        assertThat(events.get(0).payload().get("relatedName").asText()).isEqualTo("Transportadora Veloz");
        assertThat(events.get(1).payload().get("outcome").asText()).isEqualTo("Cliente aprovou o escopo");
    }
}
