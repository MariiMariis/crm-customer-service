package com.pb.crm.ticket;

import java.util.EnumSet;
import java.util.Set;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public Set<TicketStatus> allowedTransitions() {
        return switch (this) {
            case OPEN -> EnumSet.of(IN_PROGRESS, RESOLVED, CLOSED);
            case IN_PROGRESS -> EnumSet.of(OPEN, RESOLVED, CLOSED);
            case RESOLVED -> EnumSet.of(IN_PROGRESS, CLOSED);
            case CLOSED -> EnumSet.noneOf(TicketStatus.class);
        };
    }

    public boolean canTransitionTo(TicketStatus target) {
        return allowedTransitions().contains(target);
    }

    public boolean isTerminal() {
        return this == CLOSED;
    }
}
