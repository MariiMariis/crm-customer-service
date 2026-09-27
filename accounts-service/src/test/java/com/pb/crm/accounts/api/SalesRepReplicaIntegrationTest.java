package com.pb.crm.accounts.api;

import com.pb.crm.accounts.domain.salesrep.SalesRepRef;
import com.pb.crm.accounts.domain.salesrep.SalesRepRefRepository;
import com.pb.crm.accounts.support.IntegrationTestSupport;
import com.pb.crm.commons.messaging.EventEnvelope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class SalesRepReplicaIntegrationTest extends IntegrationTestSupport {

    private static final String EXCHANGE = "team.events";

    @Autowired
    private SalesRepRefRepository repository;

    private static Map<String, Object> salesRep(long id, String name, boolean active) {
        return Map.of("id", id, "name", name, "email", "rep" + id + "@pbtech.com.br", "team", "FIELD_SALES",
                "role", "REP", "active", active, "archived", false);
    }

    private Optional<String> nameOf(long id) {
        return repository.findById(id).map(SalesRepRef::name);
    }

    @Test
    void replicaFollowsEventsAndIgnoresOlderOnes() {
        Instant base = Instant.now();
        publish(EXCHANGE, envelope("team.salesrep.registered", "SalesRep", 3001L, salesRep(3001L, "Tiago Alves", true), base));
        await().atMost(Duration.ofSeconds(15)).until(() -> nameOf(3001L).isPresent());

        publish(EXCHANGE, envelope("team.salesrep.updated", "SalesRep", 3001L,
                salesRep(3001L, "Tiago Alves Pereira", true), base.plusSeconds(10)));
        await().atMost(Duration.ofSeconds(15)).until(() -> nameOf(3001L).orElse("").equals("Tiago Alves Pereira"));

        publish(EXCHANGE, envelope("team.salesrep.updated", "SalesRep", 3001L,
                salesRep(3001L, "Nome Antigo", false), base.plusSeconds(5)));
        await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3)).until(() -> true);

        SalesRepRef replica = repository.findById(3001L).orElseThrow();
        assertThat(replica.name()).isEqualTo("Tiago Alves Pereira");
        assertThat(replica.active()).isTrue();
    }

    @Test
    void duplicatedEventIsAppliedOnlyOnce() {
        Instant base = Instant.now();
        EventEnvelope registered = envelope("team.salesrep.registered", "SalesRep", 3002L,
                salesRep(3002L, "Lara Gomes", true), base);
        publish(EXCHANGE, registered);
        await().atMost(Duration.ofSeconds(15)).until(() -> nameOf(3002L).isPresent());

        publish(EXCHANGE, envelope("team.salesrep.status-changed", "SalesRep", 3002L,
                salesRep(3002L, "Lara Gomes", false), base.plusSeconds(10)));
        await().atMost(Duration.ofSeconds(15)).until(() -> !repository.findById(3002L).orElseThrow().active());

        EventEnvelope replayWithNewerClock = new EventEnvelope(registered.eventId(), registered.eventType(),
                registered.eventVersion(), base.plusSeconds(60), registered.source(), registered.aggregateType(),
                registered.aggregateId(), registered.correlationId(), null, registered.actor(), registered.payload());
        publish(EXCHANGE, replayWithNewerClock);
        await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3)).until(() -> true);

        assertThat(repository.findById(3002L).orElseThrow().active()).isFalse();
    }
}
