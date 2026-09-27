package com.pb.crm.catalog.application.product.dto;

import com.pb.crm.catalog.domain.product.BillingType;
import com.pb.crm.catalog.domain.product.ProductCategory;
import com.pb.crm.catalog.domain.product.ProductSubcategory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public record CategoryTaxonomyResponse(
        ProductCategory category,
        String skuPrefix,
        boolean hasWarranty,
        Set<BillingType> allowedBilling,
        List<ProductSubcategory> subcategories
) {
    public static List<CategoryTaxonomyResponse> all() {
        return Arrays.stream(ProductCategory.values())
                .map(category -> new CategoryTaxonomyResponse(
                        category,
                        category.skuPrefix(),
                        category.hasWarranty(),
                        category.allowedBilling(),
                        Arrays.stream(ProductSubcategory.values())
                                .filter(subcategory -> subcategory.category() == category)
                                .toList()))
                .toList();
    }
}
