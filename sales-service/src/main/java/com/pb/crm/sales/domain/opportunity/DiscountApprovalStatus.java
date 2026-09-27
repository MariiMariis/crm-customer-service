package com.pb.crm.sales.domain.opportunity;

public enum DiscountApprovalStatus {
    NOT_REQUIRED,
    PENDING,
    APPROVED,
    REJECTED;

    public boolean blocksClosing() {
        return this == PENDING || this == REJECTED;
    }
}
