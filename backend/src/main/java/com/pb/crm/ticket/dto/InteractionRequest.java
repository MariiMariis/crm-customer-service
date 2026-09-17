package com.pb.crm.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record InteractionRequest(
        @NotBlank(message = "autor e obrigatorio")
        String author,

        @NotBlank(message = "mensagem e obrigatoria")
        String message
) {
}
