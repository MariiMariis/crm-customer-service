package com.pb.crm.catalog.api;

import com.pb.crm.catalog.support.IntegrationTestSupport;
import com.pb.crm.commons.messaging.EventEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductEventsIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    private String probe;

    @BeforeEach
    void bindProbeQueue() {
        probe = bindProbe("catalog.events", "catalog.product.#");
    }

    @Test
    void productLifecyclePublishesCreatedUpdatedAndStatusChanged() throws Exception {
        Map<String, Object> product = new HashMap<>();
        product.put("name", "Licenca Veeam Backup");
        product.put("subcategory", "INFRASTRUCTURE");
        product.put("billing", "ANNUAL");
        product.put("unit", "LICENSE");
        product.put("unitPrice", 1800.00);
        product.put("maxDiscountPercent", 12);
        String body = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testObjectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = testObjectMapper.readTree(body).get("id").asLong();

        product.put("unitPrice", 1950.00);
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testObjectMapper.writeValueAsString(product)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/products/{id}/archive", id)).andExpect(status().isOk());

        List<EventEnvelope> received = new ArrayList<>();
        await().atMost(Duration.ofSeconds(15)).until(() -> {
            receive(probe).ifPresent(received::add);
            return received.stream().filter(e -> e.aggregateId().equals(String.valueOf(id))).count() >= 3;
        });
        List<EventEnvelope> events = received.stream().filter(e -> e.aggregateId().equals(String.valueOf(id))).toList();

        assertThat(events).extracting(EventEnvelope::eventType)
                .containsExactly("catalog.product.created", "catalog.product.updated", "catalog.product.status-changed");
        assertThat(events.get(1).payload().get("unitPrice").decimalValue()).isEqualByComparingTo("1950.00");
        assertThat(events.get(2).payload().get("archived").asBoolean()).isTrue();
        assertThat(events.get(0).payload().get("billing").asText()).isEqualTo("ANNUAL");
    }
}
