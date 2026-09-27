package com.pb.crm.sales.domain.reference;

import com.pb.crm.sales.domain.opportunity.BillingType;

import java.math.BigDecimal;

public record ProductRef(
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
    public boolean isSellable() {
        return active && !archived;
    }
}
