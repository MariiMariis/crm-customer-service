package com.pb.crm.catalog.application.product;

import com.pb.crm.catalog.domain.product.BillingType;
import com.pb.crm.catalog.domain.product.Product;
import com.pb.crm.catalog.domain.product.ProductCategory;
import com.pb.crm.catalog.domain.product.ProductDetails;
import com.pb.crm.catalog.domain.product.ProductSubcategory;
import com.pb.crm.catalog.domain.product.UnitOfMeasure;

import java.math.BigDecimal;

public record ProductSnapshot(
        Long id,
        String sku,
        String name,
        ProductCategory category,
        ProductSubcategory subcategory,
        BillingType billing,
        UnitOfMeasure unit,
        BigDecimal unitPrice,
        BigDecimal maxDiscountPercent,
        String manufacturer,
        boolean active,
        boolean archived,
        Long version
) {
    public static ProductSnapshot from(Product product) {
        ProductDetails details = product.getDetails();
        return new ProductSnapshot(
                product.getId(),
                product.getSku().value(),
                details.name(),
                product.getCategory(),
                details.subcategory(),
                details.billing(),
                details.unit(),
                details.unitPrice(),
                details.maxDiscountPercent(),
                details.manufacturer(),
                product.isActive(),
                product.isArchived(),
                product.getVersion()
        );
    }
}
