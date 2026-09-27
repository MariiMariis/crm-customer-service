package com.pb.crm.sales.application.lead.dto;

import jakarta.validation.constraints.NotNull;

public record AssignLeadRequest(
        @NotNull(message = "vendedor responsavel e obrigatorio")
        Long ownerId
) {
}
