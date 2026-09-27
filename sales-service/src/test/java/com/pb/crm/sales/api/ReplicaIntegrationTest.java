package com.pb.crm.sales.api;

import com.pb.crm.sales.domain.opportunity.BillingType;
import com.pb.crm.sales.domain.reference.ProductRef;
import com.pb.crm.sales.domain.reference.ReferenceRepository;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import com.pb.crm.sales.domain.reference.SalesRepRefRepository;
import com.pb.crm.sales.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ReplicaIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private SalesRepRefRepository salesRepRefRepository;

    @Autowired
    private ReferenceRepository referenceRepository;

    @Test
    void salesRepEventsFeedTheReplicaIncludingTheManager() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", 4001L);
        payload.put("name", "Otavio Reis");
        payload.put("email", "otavio@pbtech.com.br");
        payload.put("managerId", 4000L);
        payload.put("active", true);
        payload.put("archived", false);
        publish("team.events", envelope("team.salesrep.registered", "SalesRep", 4001L, payload, Instant.now()));

        await().atMost(Duration.ofSeconds(15)).until(() -> salesRepRefRepository.findById(4001L).isPresent());
        SalesRepRef replica = salesRepRefRepository.findById(4001L).orElseThrow();
        assertThat(replica.managerId()).isEqualTo(4000L);
        assertThat(replica.canOwnRecords()).isTrue();
    }

    @Test
    void productEventsFeedTheReplicaAndDeactivationBlocksSales() {
        Instant base = Instant.now();
        publish("catalog.events", envelope("catalog.product.created", "Product", 5001L,
                product(5001L, true, "4500.00"), base));
        await().atMost(Duration.ofSeconds(15)).until(() -> referenceRepository.findProduct(5001L).isPresent());

        publish("catalog.events", envelope("catalog.product.status-changed", "Product", 5001L,
                product(5001L, false, "4500.00"), base.plusSeconds(5)));
        await().atMost(Duration.ofSeconds(15)).until(() -> !referenceRepository.findProduct(5001L).orElseThrow().isSellable());

        ProductRef replica = referenceRepository.findProduct(5001L).orElseThrow();
        assertThat(replica.sku()).isEqualTo("HW-SRV-005001");
        assertThat(replica.billing()).isEqualTo(BillingType.ONE_TIME);
        assertThat(replica.unitPrice()).isEqualByComparingTo("4500.00");
    }

    private static Map<String, Object> product(long id, boolean active, String price) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("sku", "HW-SRV-00" + id);
        payload.put("name", "Servidor Dell PowerEdge R760");
        payload.put("category", "HARDWARE");
        payload.put("subcategory", "SERVER");
        payload.put("billing", "ONE_TIME");
        payload.put("unit", "UNIT");
        payload.put("unitPrice", price);
        payload.put("maxDiscountPercent", "7.00");
        payload.put("manufacturer", "Dell");
        payload.put("active", active);
        payload.put("archived", false);
        return payload;
    }
}
