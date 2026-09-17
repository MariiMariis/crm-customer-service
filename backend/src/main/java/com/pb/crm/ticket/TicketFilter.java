package com.pb.crm.ticket;

import java.time.Instant;

public record TicketFilter(
        TicketStatus status,
        TicketPriority priority,
        Long customerId,
        Long agentId,
        String subject,
        Instant createdFrom,
        Instant createdTo
) {
    public static TicketFilter empty() {
        return new TicketFilter(null, null, null, null, null, null, null);
    }
}
