package com.pb.crm.catalog.domain.product;

import com.pb.crm.commons.domain.AggregateRoot;
import com.pb.crm.commons.domain.AuditInfo;
import com.pb.crm.commons.error.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public class Product extends AggregateRoot {

    public static final String CREATED = "product.created";
    public static final String UPDATED = "product.updated";
    public static final String STATUS_CHANGED = "product.status-changed";

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private Sku sku;
    private ProductDetails details;
    private boolean active;

    private Product() {
    }

    public static Product register(Sku sku, ProductDetails details) {
        if (sku == null) {
            throw new IllegalArgumentException("SKU e obrigatorio");
        }
        Product product = new Product();
        product.details = validate(details);
        if (!sku.value().startsWith(product.getCategory().skuPrefix() + "-" + details.subcategory().code() + "-")) {
            throw new BusinessRuleException("o SKU nao corresponde a categoria e subcategoria do produto");
        }
        product.sku = sku;
        product.active = true;
        product.recordEvent(CREATED);
        return product;
    }

    public static Product rehydrate(Long id,
                                    Sku sku,
                                    ProductDetails details,
                                    boolean active,
                                    boolean archived,
                                    Instant archivedAt,
                                    AuditInfo audit) {
        Product product = new Product();
        product.rehydrateBase(id, audit, archived, archivedAt);
        product.sku = sku;
        product.details = details;
        product.active = active;
        return product;
    }

    public void update(ProductDetails newDetails) {
        assertNotArchived();
        ProductDetails validated = validate(newDetails);
        if (validated.subcategory() != details.subcategory()) {
            throw new BusinessRuleException("a subcategoria nao pode ser alterada porque compoe o SKU; cadastre um novo produto");
        }
        this.details = validated;
        recordEvent(UPDATED);
    }

    public void activate() {
        assertNotArchived();
        if (active) {
            throw new BusinessRuleException("o produto ja esta ativo");
        }
        active = true;
        recordEvent(STATUS_CHANGED);
    }

    public void deactivate() {
        assertNotArchived();
        if (!active) {
            throw new BusinessRuleException("o produto ja esta inativo");
        }
        active = false;
        recordEvent(STATUS_CHANGED);
    }

    @Override
    public void archive() {
        super.archive();
        active = false;
        recordEvent(STATUS_CHANGED);
    }

    @Override
    public void restore() {
        super.restore();
        recordEvent(STATUS_CHANGED);
    }

    public boolean isSellable() {
        return active && !isArchived();
    }

    public BigDecimal marginPercent() {
        if (details.unitCost() == null || details.unitPrice().signum() == 0) {
            return null;
        }
        return details.unitPrice().subtract(details.unitCost())
                .multiply(HUNDRED)
                .divide(details.unitPrice(), 2, RoundingMode.HALF_UP);
    }

    private static ProductDetails validate(ProductDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("dados do produto sao obrigatorios");
        }
        String name = requireText(details.name(), "nome");
        requireValue(details.subcategory(), "subcategoria");
        requireValue(details.billing(), "tipo de cobranca");
        requireValue(details.unit(), "unidade de medida");
        requireValue(details.unitPrice(), "preco de lista");
        requireValue(details.maxDiscountPercent(), "desconto maximo");
        ProductCategory category = details.subcategory().category();
        if (!category.allows(details.billing())) {
            throw new BusinessRuleException("a categoria %s aceita apenas as cobrancas %s"
                    .formatted(category, category.allowedBilling()));
        }
        if (details.unitPrice().signum() <= 0) {
            throw new BusinessRuleException("o preco de lista deve ser maior que zero");
        }
        if (details.unitCost() != null && details.unitCost().signum() < 0) {
            throw new BusinessRuleException("o custo nao pode ser negativo");
        }
        if (details.maxDiscountPercent().signum() < 0 || details.maxDiscountPercent().compareTo(HUNDRED) > 0) {
            throw new BusinessRuleException("o desconto maximo deve estar entre 0 e 100%");
        }
        Integer warranty = details.warrantyMonths();
        if (warranty != null && !category.hasWarranty()) {
            throw new BusinessRuleException("apenas produtos de hardware possuem garantia");
        }
        if (warranty != null && warranty < 0) {
            throw new BusinessRuleException("a garantia nao pode ser negativa");
        }
        return new ProductDetails(
                name,
                blankToNull(details.description()),
                details.subcategory(),
                details.billing(),
                details.unit(),
                details.unitPrice(),
                details.unitCost(),
                details.maxDiscountPercent(),
                blankToNull(details.manufacturer()),
                warranty
        );
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " e obrigatorio");
        }
        return value.trim();
    }

    private static void requireValue(Object value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " e obrigatorio");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Sku getSku() {
        return sku;
    }

    public ProductDetails getDetails() {
        return details;
    }

    public ProductCategory getCategory() {
        return details.subcategory().category();
    }

    public boolean isActive() {
        return active;
    }
}
