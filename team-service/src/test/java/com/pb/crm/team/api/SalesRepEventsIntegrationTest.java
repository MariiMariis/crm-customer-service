package com.pb.crm.team.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.pb.crm.commons.messaging.EventEnvelope;
import com.pb.crm.team.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SalesRepEventsIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    private String probe;

    @BeforeEach
    void bindProbeQueue() {
        probe = bindProbe("team.events", "team.salesrep.#");
    }

    private List<EventEnvelope> drain(int expected) {
        List<EventEnvelope> received = new ArrayList<>();
        await().atMost(Duration.ofSeconds(15)).until(() -> {
            receive(probe).ifPresent(received::add);
            return received.size() >= expected;
        });
        return received;
    }

    @Test
    void registeringAndDeactivatingPublishesSnapshotEventsThroughTheOutbox() throws Exception {
        String email = "evento." + UUID.randomUUID().toString().substring(0, 8) + "@pbtech.com.br";
        String body = mockMvc.perform(post("/api/sales-reps")
                        .header("X-Actor", "gestora.rh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testObjectMapper.writeValueAsString(Map.of(
                                "name", "Rafaela Mendes",
                                "email", email,
                                "team", "INSIDE_SALES",
                                "role", "REP",
                                "monthlyQuota", 60000))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = testObjectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(post("/api/sales-reps/{id}/deactivate", id).header("X-Actor", "gestora.rh"))
                .andExpect(status().isOk());

        List<EventEnvelope> events = drain(2).stream()
                .filter(event -> event.aggregateId().equals(String.valueOf(id)))
                .toList();

        assertThat(events).extracting(EventEnvelope::eventType)
                .containsExactly("team.salesrep.registered", "team.salesrep.status-changed");
        EventEnvelope registered = events.get(0);
        assertThat(registered.source()).isEqualTo("team-service");
        assertThat(registered.aggregateType()).isEqualTo("SalesRep");
        assertThat(registered.actor()).isEqualTo("gestora.rh");
        JsonNode payload = registered.payload();
        assertThat(payload.get("email").asText()).isEqualTo(email);
        assertThat(payload.get("active").asBoolean()).isTrue();
        assertThat(events.get(1).payload().get("active").asBoolean()).isFalse();
    }

    @Test
    void rejectedCommandPublishesNothing() throws Exception {
        String email = "sem.meta." + UUID.randomUUID().toString().substring(0, 8) + "@pbtech.com.br";
        mockMvc.perform(post("/api/sales-reps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testObjectMapper.writeValueAsString(Map.of(
                                "name", "Sem Meta",
                                "email", email,
                                "team", "INSIDE_SALES",
                                "role", "REP"))))
                .andExpect(status().isConflict());

        List<EventEnvelope> received = new ArrayList<>();
        await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3)).until(() -> {
            receive(probe).ifPresent(received::add);
            return true;
        });
        assertThat(received).noneMatch(event -> email.equals(event.payload().path("email").asText()));
    }
}
