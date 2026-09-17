package com.pb.crm.ticket.dto;

import com.pb.crm.ticket.TicketStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketStatusRequest(
        @NotNull(message = "status e obrigatorio")
        TicketStatus status,

        @Size(max = 500, message = "motivo deve ter ate 500 caracteres")
        String reason
) {
}
