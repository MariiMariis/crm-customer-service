package com.pb.crm.sales.infrastructure.messaging;

import com.pb.crm.sales.domain.opportunity.BillingType;

import java.math.BigDecimal;

public final class ReplicaPayloads {

    private ReplicaPayloads() {
    }

    public record SalesRep(
            Long id,
            String name,
            String email,
            Long managerId,
            boolean active,
            boolean archived
    ) {
    }

    public record Product(
            Long id,
            String sku,
            String name,
            String category,
            BillingType billing,
            BigDecimal unitPrice,
            BigDecimal maxDiscountPercent,
            boolean active,
            boolean archived
    ) {
    }
}
