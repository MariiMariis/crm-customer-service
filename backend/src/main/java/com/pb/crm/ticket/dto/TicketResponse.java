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
        Long agentId,
        Instant createdAt,
        Instant updatedAt,
        List<InteractionResponse> interactions
) {
    public static TicketResponse fromEntity(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCustomerId(),
                ticket.getAgentId(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getInteractions().stream().map(InteractionResponse::fromEntity).toList()
        );
    }

    /** Versao resumida (sem interacoes) para listagens. */
    public static TicketResponse summaryFromEntity(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCustomerId(),
                ticket.getAgentId(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                List.of()
        );
    }
}
