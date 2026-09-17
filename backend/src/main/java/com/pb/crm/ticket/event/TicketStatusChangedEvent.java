package com.pb.crm.ticket.event;

import com.pb.crm.ticket.TicketStatus;

public record TicketStatusChangedEvent(
        TicketSnapshot ticket,
        TicketStatus fromStatus,
        TicketStatus toStatus,
        String reason,
        String actor
) {
}
