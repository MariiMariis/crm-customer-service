package com.pb.crm.notification.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.pb.crm.notification.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationFlowIntegrationTest extends IntegrationTestSupport {

    private static final AtomicLong IDS = new AtomicLong(20_000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void salesRep(long id, String name, String email, Long managerId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("name", name);
        payload.put("email", email);
        payload.put("managerId", managerId);
        payload.put("active", true);
        payload.put("archived", false);
        publish("team.events", envelope("team.salesrep.registered", "SalesRep", id, payload, Instant.now()));
    }

    private void opportunityEvent(String type, long opportunityId, long ownerId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", opportunityId);
        payload.put("title", "Datacenter hiperconvergente");
        payload.put("companyName", "Banco Regional Sul");
        payload.put("ownerId", ownerId);
        payload.put("ownerName", "Vendedor");
        payload.put("amount", new BigDecimal("480000.00"));
        payload.put("lossReason", "concorrente com prazo menor");
        publish("sales.events", envelope(type, "Opportunity", opportunityId, payload, Instant.now()));
    }

    private JsonNode inbox(long recipientId, String extraParam, String value) throws Exception {
        var request = get("/api/notifications").param("recipientId", String.valueOf(recipientId)).param("pageSize", "50");
        if (extraParam != null) {
            request = request.param(extraParam, value);
        }
        return testObjectMapper.readTree(mockMvc.perform(request).andReturn().getResponse().getContentAsString());
    }

    private long count(long recipientId, String param, String value) throws Exception {
        return inbox(recipientId, param, value).get("totalElements").asLong();
    }

    private void awaitRecipient(long id) {
        await().atMost(Duration.ofSeconds(15)).until(() -> {
            Long total = jdbcTemplate.queryForObject("select count(*) from recipients where id = ?", Long.class, id);
            return total != null && total == 1;
        });
    }

    @Test
    void wonOpportunityNotifiesOwnerAndManagerInAppAndByEmailThroughTheWorkQueue() throws Exception {
        long manager = IDS.incrementAndGet();
        long owner = IDS.incrementAndGet();
        salesRep(manager, "Gestora Comercial", "gestora" + manager + "@pbtech.com.br", null);
        salesRep(owner, "Vendedor Campo", "vendedor" + owner + "@pbtech.com.br", manager);
        awaitRecipient(manager);
        awaitRecipient(owner);

        opportunityEvent("sales.opportunity.won", IDS.incrementAndGet(), owner);

        await().atMost(Duration.ofSeconds(15)).until(() -> count(owner, "status", "SENT") == 2 && count(manager, "status", "SENT") == 2);

        JsonNode ownerInbox = inbox(owner, "channel", "IN_APP");
        assertThat(ownerInbox.get("content").get(0).get("type").asText()).isEqualTo("OPPORTUNITY_WON");
        assertThat(ownerInbox.get("content").get(0).get("message").asText()).contains("Banco Regional Sul").contains("R$ 480.000,00");

        mockMvc.perform(get("/api/notifications/unread-count").param("recipientId", String.valueOf(owner)))
                .andExpect(jsonPath("$.unread").value(1));
        long notificationId = ownerInbox.get("content").get(0).get("id").asLong();
        mockMvc.perform(post("/api/notifications/{id}/read", notificationId).param("recipientId", String.valueOf(manager)))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/notifications/{id}/read", notificationId).param("recipientId", String.valueOf(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unread").value(false));
        mockMvc.perform(post("/api/notifications/read-all").param("recipientId", String.valueOf(manager)))
                .andExpect(jsonPath("$.marked").value(1));
    }

    @Test
    void discountApprovalGoesOnlyToManagerAndDisabledEmailIsSkipped() throws Exception {
        long manager = IDS.incrementAndGet();
        long owner = IDS.incrementAndGet();
        salesRep(manager, "Gestor Inside", "gestor" + manager + "@pbtech.com.br", null);
        salesRep(owner, "Vendedora Inside", "vendedora" + owner + "@pbtech.com.br", manager);
        awaitRecipient(manager);
        awaitRecipient(owner);
        mockMvc.perform(put("/api/notification-preferences/{id}", manager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailEnabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailEnabled").value(false));

        opportunityEvent("sales.opportunity.discount-approval-requested", IDS.incrementAndGet(), owner);

        await().atMost(Duration.ofSeconds(15)).until(() -> count(manager, null, null) == 2);
        assertThat(count(manager, "status", "SKIPPED")).isEqualTo(1);
        assertThat(count(manager, "type", "DISCOUNT_APPROVAL_REQUESTED")).isEqualTo(2);
        assertThat(count(owner, null, null)).isZero();
    }

    @Test
    void unreachableEmailIsRetriedThenParkedInTheDeadLetterQueue() throws Exception {
        long owner = IDS.incrementAndGet();
        salesRep(owner, "Destinatario Invalido", "quebrado" + owner + "@smtp.invalid", null);
        awaitRecipient(owner);

        opportunityEvent("sales.opportunity.lost", IDS.incrementAndGet(), owner);

        await().atMost(Duration.ofSeconds(20)).until(() -> {
            var info = testAmqpAdmin.getQueueInfo("notification.dispatch.dlq");
            return info != null && info.getMessageCount() >= 1;
        });
        assertThat(count(owner, "status", "PENDING")).isEqualTo(1);
        assertThat(count(owner, "channel", "IN_APP")).isEqualTo(1);

        JsonNode status = testObjectMapper.readTree(mockMvc.perform(get("/api/messaging/status"))
                .andReturn().getResponse().getContentAsString());
        assertThat(status.get("queues").findValues("deadLetters").stream().mapToLong(JsonNode::asLong).sum())
                .isGreaterThanOrEqualTo(1);
        testAmqpAdmin.purgeQueue("notification.dispatch.dlq", false);
    }

    @Test
    void alertForOwnerNotYetSyncedIsRetriedUntilTheReplicaArrives() throws Exception {
        long owner = IDS.incrementAndGet();

        opportunityEvent("sales.opportunity.won", IDS.incrementAndGet(), owner);
        await().pollDelay(Duration.ofMillis(300)).atMost(Duration.ofSeconds(1)).until(() -> true);
        salesRep(owner, "Vendedor Recem Contratado", "novo" + owner + "@pbtech.com.br", null);

        await().atMost(Duration.ofSeconds(15)).until(() -> count(owner, "channel", "IN_APP") == 1);
    }

    @Test
    void leadAssignedNotifiesOnlyTheOwner() throws Exception {
        long manager = IDS.incrementAndGet();
        long owner = IDS.incrementAndGet();
        salesRep(manager, "Gestor", "g" + manager + "@pbtech.com.br", null);
        salesRep(owner, "SDR", "sdr" + owner + "@pbtech.com.br", manager);
        awaitRecipient(owner);
        awaitRecipient(manager);

        Map<String, Object> lead = Map.of("id", 77L, "fullName", "Renata Lopes", "companyName", "Lojas Alfa",
                "score", 90, "ownerId", owner, "ownerName", "SDR");
        publish("sales.events", envelope("sales.lead.assigned", "Lead", 77L, lead, Instant.now()));

        await().atMost(Duration.ofSeconds(15)).until(() -> count(owner, "type", "LEAD_ASSIGNED") == 2);
        assertThat(count(manager, null, null)).isZero();
        mockMvc.perform(get("/api/notifications/stats")).andExpect(status().isOk());
    }
}
