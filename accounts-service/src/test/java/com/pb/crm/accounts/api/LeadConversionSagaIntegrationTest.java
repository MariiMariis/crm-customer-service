package com.pb.crm.accounts.api;

import com.pb.crm.accounts.domain.company.Cnpj;
import com.pb.crm.accounts.support.IntegrationTestSupport;
import com.pb.crm.commons.messaging.EventEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LeadConversionSagaIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    private String replies;
    private String companyEvents;
    private final List<EventEnvelope> received = new ArrayList<>();

    @BeforeEach
    void bindProbes() {
        replies = bindProbe("accounts.events", "accounts.lead-account.*");
        companyEvents = bindProbe("accounts.events", "accounts.company.created");
        received.clear();
    }

    private static String randomCnpj() {
        return Cnpj.fromBase(Long.toString(ThreadLocalRandom.current().nextLong(100_000_000_000L, 999_999_999_999L))).formatted();
    }

    private static Map<String, Object> request(long leadId, String cnpj, String email) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadId);
        payload.put("companyName", "Construtora Horizonte Azul");
        payload.put("cnpj", cnpj);
        payload.put("industry", "MANUFACTURING");
        payload.put("companySize", "MEDIUM");
        payload.put("city", "Curitiba");
        payload.put("state", "PR");
        payload.put("firstName", "Julia");
        payload.put("lastName", "Martins");
        payload.put("email", email);
        payload.put("phone", "41999990000");
        payload.put("jobTitle", "Gerente de TI");
        payload.put("ownerId", null);
        payload.put("createOpportunity", true);
        return payload;
    }

    private static String uniqueEmail() {
        return "julia." + UUID.randomUUID().toString().substring(0, 8) + "@horizonteazul.com.br";
    }

    private EventEnvelope awaitReply(long leadId) {
        await().atMost(Duration.ofSeconds(15)).until(() -> {
            receive(replies).ifPresent(received::add);
            return findReply(leadId).isPresent();
        });
        return findReply(leadId).orElseThrow();
    }

    private Optional<EventEnvelope> findReply(long leadId) {
        return received.stream().filter(e -> e.aggregateId().equals(String.valueOf(leadId))).findFirst();
    }

    private void requestConversion(long leadId, String cnpj, String email) {
        publish("sales.events", envelope("sales.lead.conversion-requested", "Lead", leadId,
                request(leadId, cnpj, email), Instant.now()));
    }

    @Test
    void createsCompanyAndPrimaryContactAndRepliesProvisioned() throws Exception {
        String cnpj = randomCnpj();
        String email = uniqueEmail();
        requestConversion(9001L, cnpj, email);

        EventEnvelope reply = awaitReply(9001L);
        assertThat(reply.eventType()).isEqualTo("accounts.lead-account.provisioned");
        assertThat(reply.payload().get("companyCreated").asBoolean()).isTrue();
        assertThat(reply.payload().get("contactCreated").asBoolean()).isTrue();
        long companyId = reply.payload().get("companyId").asLong();

        mockMvc.perform(get("/api/companies/{id}", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").value(cnpj))
                .andExpect(jsonPath("$.type").value("PROSPECT"))
                .andExpect(jsonPath("$.primaryContactName").value("Julia Martins"))
                .andExpect(jsonPath("$.createdBy").value("integration-test"));

        await().atMost(Duration.ofSeconds(15)).until(() -> receive(companyEvents)
                .filter(e -> e.aggregateId().equals(String.valueOf(companyId))).isPresent());
    }

    @Test
    void reusesExistingCompanyByCnpjAndExistingContactOfTheSameCompany() {
        String cnpj = randomCnpj();
        String email = uniqueEmail();
        requestConversion(9002L, cnpj, email);
        long companyId = awaitReply(9002L).payload().get("companyId").asLong();

        requestConversion(9003L, cnpj, email);
        EventEnvelope second = awaitReply(9003L);

        assertThat(second.eventType()).isEqualTo("accounts.lead-account.provisioned");
        assertThat(second.payload().get("companyId").asLong()).isEqualTo(companyId);
        assertThat(second.payload().get("companyCreated").asBoolean()).isFalse();
        assertThat(second.payload().get("contactCreated").asBoolean()).isFalse();
    }

    @Test
    void rejectsInvalidCnpjAndEmailOwnedByAnotherCompany() {
        requestConversion(9004L, "11.222.333/0001-00", uniqueEmail());
        EventEnvelope invalid = awaitReply(9004L);
        assertThat(invalid.eventType()).isEqualTo("accounts.lead-account.rejected");
        assertThat(invalid.payload().get("reason").asText()).contains("CNPJ");

        String email = uniqueEmail();
        requestConversion(9005L, randomCnpj(), email);
        awaitReply(9005L);
        requestConversion(9006L, randomCnpj(), email);
        EventEnvelope conflict = awaitReply(9006L);
        assertThat(conflict.eventType()).isEqualTo("accounts.lead-account.rejected");
        assertThat(conflict.payload().get("reason").asText()).contains("outra empresa");
    }
}
