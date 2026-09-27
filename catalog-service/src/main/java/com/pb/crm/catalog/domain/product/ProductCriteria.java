package com.pb.crm.catalog.domain.product;

public record ProductCriteria(
        String term,
        ProductCategory category,
        ProductSubcategory subcategory,
        BillingType billing,
        Boolean active,
        boolean includeArchived
) {
}
