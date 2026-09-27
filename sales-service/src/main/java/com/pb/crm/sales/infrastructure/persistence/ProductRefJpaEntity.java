package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.opportunity.BillingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product_refs")
public class ProductRefJpaEntity {

    @Id
    private Long id;

    @Column(nullable = false, length = 20)
    private String sku;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 20)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingType billing;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "max_discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxDiscountPercent;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean archived;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    protected ProductRefJpaEntity() {
    }

    public ProductRefJpaEntity(Long id) {
        this.id = id;
    }

    public void apply(String sku,
                      String name,
                      String category,
                      BillingType billing,
                      BigDecimal unitPrice,
                      BigDecimal maxDiscountPercent,
                      boolean active,
                      boolean archived) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.billing = billing;
        this.unitPrice = unitPrice;
        this.maxDiscountPercent = maxDiscountPercent;
        this.active = active;
        this.archived = archived;
        this.syncedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BillingType getBilling() {
        return billing;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getMaxDiscountPercent() {
        return maxDiscountPercent;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isArchived() {
        return archived;
    }
}
