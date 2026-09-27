package com.pb.crm.catalog.infrastructure.persistence;

import com.pb.crm.catalog.domain.product.Product;
import com.pb.crm.catalog.domain.product.ProductDetails;
import com.pb.crm.catalog.domain.product.Sku;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toDomain(ProductJpaEntity entity) {
        ProductDetails details = new ProductDetails(
                entity.getName(),
                entity.getDescription(),
                entity.getSubcategory(),
                entity.getBilling(),
                entity.getUnit(),
                entity.getUnitPrice(),
                entity.getUnitCost(),
                entity.getMaxDiscountPercent(),
                entity.getManufacturer(),
                entity.getWarrantyMonths()
        );
        return Product.rehydrate(
                entity.getId(),
                new Sku(entity.getSku()),
                details,
                entity.isActive(),
                entity.isArchived(),
                entity.getArchivedAt(),
                entity.toAuditInfo()
        );
    }

    public void copyToEntity(Product product, ProductJpaEntity entity) {
        ProductDetails details = product.getDetails();
        entity.setSku(product.getSku().value());
        entity.setName(details.name());
        entity.setDescription(details.description());
        entity.setCategory(product.getCategory());
        entity.setSubcategory(details.subcategory());
        entity.setBilling(details.billing());
        entity.setUnit(details.unit());
        entity.setUnitPrice(details.unitPrice());
        entity.setUnitCost(details.unitCost());
        entity.setMaxDiscountPercent(details.maxDiscountPercent());
        entity.setManufacturer(details.manufacturer());
        entity.setWarrantyMonths(details.warrantyMonths());
        entity.setActive(product.isActive());
        entity.applyArchiveState(product.isArchived(), product.getArchivedAt());
    }
}
