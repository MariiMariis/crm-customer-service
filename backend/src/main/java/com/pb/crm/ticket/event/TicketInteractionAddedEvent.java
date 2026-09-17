package com.pb.crm.ticket.event;

public record TicketInteractionAddedEvent(
        TicketSnapshot ticket,
        String author,
        String message,
        String actor
) {
}
