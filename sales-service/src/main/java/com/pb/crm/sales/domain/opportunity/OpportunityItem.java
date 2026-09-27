package com.pb.crm.sales.domain.opportunity;

import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.sales.domain.reference.ProductRef;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OpportunityItem {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal("12");

    private final Long id;
    private final Long productId;
    private final String sku;
    private final String productName;
    private final String category;
    private final BillingType billing;
    private final BigDecimal unitPrice;
    private final BigDecimal maxDiscountPercent;
    private int quantity;
    private BigDecimal discountPercent;

    private OpportunityItem(Long id,
                            Long productId,
                            String sku,
                            String productName,
                            String category,
                            BillingType billing,
                            BigDecimal unitPrice,
                            BigDecimal maxDiscountPercent,
                            int quantity,
                            BigDecimal discountPercent) {
        this.id = id;
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.category = category;
        this.billing = billing;
        this.unitPrice = unitPrice;
        this.maxDiscountPercent = maxDiscountPercent;
        this.quantity = quantity;
        this.discountPercent = discountPercent;
    }

    static OpportunityItem of(ProductRef product, int quantity, BigDecimal discountPercent) {
        if (!product.isSellable()) {
            throw new BusinessRuleException("o produto %s nao esta disponivel para venda".formatted(product.sku()));
        }
        OpportunityItem item = new OpportunityItem(null, product.id(), product.sku(), product.name(), product.category(),
                product.billing(), product.unitPrice(), product.maxDiscountPercent(), 1, BigDecimal.ZERO);
        item.change(quantity, discountPercent);
        return item;
    }

    public static OpportunityItem rehydrate(Long id,
                                            Long productId,
                                            String sku,
                                            String productName,
                                            String category,
                                            BillingType billing,
                                            BigDecimal unitPrice,
                                            BigDecimal maxDiscountPercent,
                                            int quantity,
                                            BigDecimal discountPercent) {
        return new OpportunityItem(id, productId, sku, productName, category, billing, unitPrice, maxDiscountPercent,
                quantity, discountPercent);
    }

    void change(int newQuantity, BigDecimal newDiscountPercent) {
        if (newQuantity < 1) {
            throw new BusinessRuleException("a quantidade deve ser maior que zero");
        }
        BigDecimal discount = newDiscountPercent == null ? BigDecimal.ZERO : newDiscountPercent;
        if (discount.signum() < 0 || discount.compareTo(HUNDRED) > 0) {
            throw new BusinessRuleException("o desconto deve estar entre 0 e 100%");
        }
        this.quantity = newQuantity;
        this.discountPercent = discount.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean exceedsDiscountLimit() {
        return discountPercent.compareTo(maxDiscountPercent) > 0;
    }

    public BigDecimal grossTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal netTotal() {
        BigDecimal factor = HUNDRED.subtract(discountPercent).divide(HUNDRED, 6, RoundingMode.HALF_UP);
        return grossTotal().multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal oneTimeValue() {
        return billing == BillingType.ONE_TIME ? netTotal() : BigDecimal.ZERO.setScale(2);
    }

    public BigDecimal monthlyValue() {
        return switch (billing) {
            case ONE_TIME -> BigDecimal.ZERO.setScale(2);
            case MONTHLY -> netTotal();
            case ANNUAL -> netTotal().divide(MONTHS_PER_YEAR, 2, RoundingMode.HALF_UP);
        };
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
}
