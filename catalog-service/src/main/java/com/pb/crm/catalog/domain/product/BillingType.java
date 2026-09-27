package com.pb.crm.catalog.domain.product;

public enum BillingType {
    ONE_TIME,
    MONTHLY,
    ANNUAL;

    public boolean isRecurring() {
        return this != ONE_TIME;
    }
}
