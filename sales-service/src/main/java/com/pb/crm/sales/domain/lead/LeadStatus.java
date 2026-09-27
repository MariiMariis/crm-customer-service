package com.pb.crm.sales.domain.lead;

import java.util.EnumSet;
import java.util.Set;

public enum LeadStatus {
    NEW,
    CONTACTED,
    QUALIFIED,
    CONVERTING,
    CONVERTED,
    UNQUALIFIED;

    public static final Set<LeadStatus> OPEN = EnumSet.of(NEW, CONTACTED, QUALIFIED, CONVERTING);

    public boolean isOpen() {
        return OPEN.contains(this);
    }

    public boolean isEditable() {
        return this == NEW || this == CONTACTED || this == QUALIFIED;
    }
}
