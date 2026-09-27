package com.pb.crm.catalog.application.product.dto;

import com.pb.crm.catalog.domain.product.BillingType;
import com.pb.crm.catalog.domain.product.Product;
import com.pb.crm.catalog.domain.product.ProductCategory;
import com.pb.crm.catalog.domain.product.ProductDetails;
import com.pb.crm.catalog.domain.product.ProductSubcategory;
import com.pb.crm.catalog.domain.product.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        ProductCategory category,
        ProductSubcategory subcategory,
        BillingType billing,
        boolean recurring,
        UnitOfMeasure unit,
        BigDecimal unitPrice,
        BigDecimal unitCost,
        BigDecimal marginPercent,
        BigDecimal maxDiscountPercent,
        String manufacturer,
        Integer warrantyMonths,
        boolean active,
        boolean sellable,
        boolean archived,
        Instant archivedAt,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        Long version
) {
    public static ProductResponse from(Product product) {
        ProductDetails details = product.getDetails();
        return new ProductResponse(
                product.getId(),
                product.getSku().value(),
                details.name(),
                details.description(),
                product.getCategory(),
                details.subcategory(),
                details.billing(),
                details.billing().isRecurring(),
                details.unit(),
                details.unitPrice(),
                details.unitCost(),
                product.marginPercent(),
                details.maxDiscountPercent(),
                details.manufacturer(),
                details.warrantyMonths(),
                product.isActive(),
                product.isSellable(),
                product.isArchived(),
                product.getArchivedAt(),
                product.getAudit().createdAt(),
                product.getAudit().updatedAt(),
                product.getAudit().createdBy(),
                product.getAudit().updatedBy(),
                product.getVersion()
        );
    }
}
