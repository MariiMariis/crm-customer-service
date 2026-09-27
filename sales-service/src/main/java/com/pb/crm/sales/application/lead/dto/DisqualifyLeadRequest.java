package com.pb.crm.sales.application.lead.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisqualifyLeadRequest(
        @NotBlank(message = "motivo e obrigatorio")
        @Size(max = 500, message = "motivo deve ter ate 500 caracteres")
        String reason
) {
}
