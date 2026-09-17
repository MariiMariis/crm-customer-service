package com.pb.crm.ticket.dto;

import com.pb.crm.ticket.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketRequest(
        @NotBlank(message = "assunto e obrigatorio")
        String subject,

        @NotBlank(message = "descricao e obrigatoria")
        String description,

        @NotNull(message = "prioridade e obrigatoria")
        TicketPriority priority,

        @NotNull(message = "customerId e obrigatorio")
        Long customerId,

        Long agentId
) {
}
