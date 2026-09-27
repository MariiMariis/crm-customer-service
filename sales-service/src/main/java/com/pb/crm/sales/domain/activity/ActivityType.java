package com.pb.crm.sales.domain.activity;

public enum ActivityType {
    TASK,
    CALL,
    MEETING,
    EMAIL;

    public boolean requiresOutcome() {
        return this == CALL || this == MEETING;
    }
}
