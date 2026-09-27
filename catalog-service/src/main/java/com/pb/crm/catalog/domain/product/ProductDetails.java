package com.pb.crm.catalog.domain.product;

import java.math.BigDecimal;

public record ProductDetails(
        String name,
        String description,
        ProductSubcategory subcategory,
        BillingType billing,
        UnitOfMeasure unit,
        BigDecimal unitPrice,
        BigDecimal unitCost,
        BigDecimal maxDiscountPercent,
        String manufacturer,
        Integer warrantyMonths
) {
}
