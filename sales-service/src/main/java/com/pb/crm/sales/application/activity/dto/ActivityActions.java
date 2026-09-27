package com.pb.crm.sales.application.activity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ActivityActions {

    private ActivityActions() {
    }

    public record Complete(
            @Size(max = 2000, message = "resultado deve ter ate 2000 caracteres")
            String outcome,

            @Min(value = 0, message = "duracao nao pode ser negativa")
            @Max(value = 1440, message = "duracao deve ser de ate 1440 minutos")
            Integer durationMinutes
    ) {
    }

    public record Cancel(
            @NotBlank(message = "motivo e obrigatorio")
            @Size(max = 500, message = "motivo deve ter ate 500 caracteres")
            String reason
    ) {
    }
}
