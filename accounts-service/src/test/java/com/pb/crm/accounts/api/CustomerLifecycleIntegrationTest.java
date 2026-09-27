package com.pb.crm.accounts.api;

import com.pb.crm.accounts.domain.company.Cnpj;
import com.pb.crm.accounts.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerLifecycleIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    private long createCompany(String type) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("legalName", "Distribuidora Litoral Ltda");
        body.put("cnpj", Cnpj.fromBase(Long.toString(ThreadLocalRandom.current().nextLong(100_000_000_000L, 999_999_999_999L))).formatted());
        body.put("industry", "LOGISTICS");
        body.put("size", "MEDIUM");
        body.put("type", type);
        String response = mockMvc.perform(post("/api/companies").contentType(MediaType.APPLICATION_JSON)
                        .content(testObjectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return testObjectMapper.readTree(response).get("id").asLong();
    }

    private String typeOf(long id) throws Exception {
        String body = mockMvc.perform(get("/api/companies/{id}", id)).andReturn().getResponse().getContentAsString();
        return testObjectMapper.readTree(body).get("type").asText();
    }

    private void opportunityWon(long opportunityId, long companyId) {
        publish("sales.events", envelope("sales.opportunity.won", "Opportunity", opportunityId,
                Map.of("id", opportunityId, "companyId", companyId, "stage", "WON"), Instant.now()));
    }

    @Test
    void wonOpportunityPromotesProspectToCustomer() throws Exception {
        long companyId = createCompany("PROSPECT");

        opportunityWon(99001L, companyId);

        await().atMost(Duration.ofSeconds(15)).until(() -> typeOf(companyId).equals("CUSTOMER"));
    }

    @Test
    void partnerIsNotChangedByWonOpportunity() throws Exception {
        long companyId = createCompany("PARTNER");

        opportunityWon(99002L, companyId);
        await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3)).until(() -> true);

        assertThat(typeOf(companyId)).isEqualTo("PARTNER");
    }
}
