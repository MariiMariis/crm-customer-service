package com.pb.crm.ticket.dto;

import com.pb.crm.ticket.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TicketStatusRequest(
        @NotNull(message = "status e obrigatorio")
        TicketStatus status
) {
}
