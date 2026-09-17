package com.pb.crm.ticket.dto;

import com.pb.crm.ticket.Ticket;
import com.pb.crm.ticket.TicketPriority;
import com.pb.crm.ticket.TicketStatus;

import java.time.Instant;
import java.util.List;

public record TicketResponse(
        Long id,
        String subject,
        String description,
        TicketStatus status,
        TicketPriority priority,
        Long customerId,
        String customerName,
        Long agentId,
        String agentName,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt,
        Instant closedAt,
        String createdBy,
        String updatedBy,
        Long version,
        List<InteractionResponse> interactions
) {
    public static TicketResponse fromEntity(Ticket ticket) {
        return build(ticket, ticket.getInteractions().stream().map(InteractionResponse::fromEntity).toList());
    }

    public static TicketResponse summaryFromEntity(Ticket ticket) {
        return build(ticket, List.of());
    }

    private static TicketResponse build(Ticket ticket, List<InteractionResponse> interactions) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCustomerId(),
                ticket.getCustomer() == null ? null : ticket.getCustomer().getName(),
                ticket.getAgentId(),
                ticket.getAgent() == null ? null : ticket.getAgent().getName(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                ticket.getCreatedBy(),
                ticket.getUpdatedBy(),
                ticket.getVersion(),
                interactions
        );
    }
}
