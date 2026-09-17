package com.pb.crm.ticket.dto;

import com.pb.crm.ticket.TicketStatus;
import com.pb.crm.ticket.TicketStatusHistory;

import java.time.Instant;

public record TicketStatusHistoryResponse(
        Long id,
        Long ticketId,
        TicketStatus fromStatus,
        TicketStatus toStatus,
        String reason,
        String changedBy,
        Instant changedAt
) {
    public static TicketStatusHistoryResponse fromEntity(TicketStatusHistory history) {
        return new TicketStatusHistoryResponse(
                history.getId(),
                history.getTicket().getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getReason(),
                history.getChangedBy(),
                history.getChangedAt()
        );
    }
}
