package com.pb.crm.ticket.event;

public record TicketCreatedEvent(TicketSnapshot ticket, String actor) {
}
