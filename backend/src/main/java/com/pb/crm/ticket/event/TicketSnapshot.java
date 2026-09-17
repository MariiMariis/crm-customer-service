package com.pb.crm.ticket.event;

import com.pb.crm.ticket.Ticket;
import com.pb.crm.ticket.TicketPriority;
import com.pb.crm.ticket.TicketStatus;

public record TicketSnapshot(
        Long id,
        String subject,
        TicketStatus status,
        TicketPriority priority,
        Long customerId,
        String customerName,
        String customerEmail,
        String customerPhone,
        String agentName
) {
    public static TicketSnapshot from(Ticket ticket) {
        return new TicketSnapshot(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCustomerId(),
                ticket.getCustomer() == null ? null : ticket.getCustomer().getName(),
                ticket.getCustomer() == null ? null : ticket.getCustomer().getEmail(),
                ticket.getCustomer() == null ? null : ticket.getCustomer().getPhone(),
                ticket.getAgent() == null ? null : ticket.getAgent().getName()
        );
    }
}
