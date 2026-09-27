package com.pb.crm.catalog.domain.product;

import java.util.EnumSet;
import java.util.Set;

public enum ProductCategory {
    SOFTWARE("SW", EnumSet.of(BillingType.ONE_TIME, BillingType.MONTHLY, BillingType.ANNUAL)),
    HARDWARE("HW", EnumSet.of(BillingType.ONE_TIME)),
    SERVICE("SV", EnumSet.of(BillingType.ONE_TIME, BillingType.MONTHLY));

    private final String skuPrefix;
    private final Set<BillingType> allowedBilling;

    ProductCategory(String skuPrefix, Set<BillingType> allowedBilling) {
        this.skuPrefix = skuPrefix;
        this.allowedBilling = allowedBilling;
    }

    public String skuPrefix() {
        return skuPrefix;
    }

    public Set<BillingType> allowedBilling() {
        return EnumSet.copyOf(allowedBilling);
    }

    public boolean allows(BillingType billing) {
        return allowedBilling.contains(billing);
    }

    public boolean hasWarranty() {
        return this == HARDWARE;
    }
}
