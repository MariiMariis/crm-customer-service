package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.opportunity.BillingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "opportunity_items")
public class OpportunityItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opportunity_items_seq")
    @SequenceGenerator(name = "opportunity_items_seq", sequenceName = "opportunity_items_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private OpportunityJpaEntity opportunity;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 20)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 160)
    private String productName;

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
    private int quantity;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "net_total", nullable = false, precision = 17, scale = 2)
    private BigDecimal netTotal;

    protected OpportunityItemJpaEntity() {
    }

    public OpportunityItemJpaEntity(OpportunityJpaEntity opportunity) {
        this.opportunity = opportunity;
    }

    public void apply(Long productId,
                      String sku,
                      String productName,
                      String category,
                      BillingType billing,
                      BigDecimal unitPrice,
                      BigDecimal maxDiscountPercent,
                      int quantity,
                      BigDecimal discountPercent,
                      BigDecimal netTotal) {
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.category = category;
        this.billing = billing;
        this.unitPrice = unitPrice;
        this.maxDiscountPercent = maxDiscountPercent;
        this.quantity = quantity;
        this.discountPercent = discountPercent;
        this.netTotal = netTotal;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getProductName() {
        return productName;
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

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public BigDecimal getNetTotal() {
        return netTotal;
    }
}
