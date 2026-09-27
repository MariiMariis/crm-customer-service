package com.pb.crm.sales.application.opportunity.dto;

import com.pb.crm.sales.domain.opportunity.OpportunityStage;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class OpportunityActions {

    private OpportunityActions() {
    }

    public record ChangeStage(
            @NotNull(message = "etapa e obrigatoria")
            OpportunityStage stage
    ) {
    }

    public record AdjustProbability(
            @Min(value = 1, message = "probabilidade minima e 1%")
            @Max(value = 99, message = "probabilidade maxima e 99%")
            int probability
    ) {
    }

    public record Lose(
            @NotBlank(message = "motivo da perda e obrigatorio")
            @Size(max = 500, message = "motivo deve ter ate 500 caracteres")
            String reason
    ) {
    }

    public record DiscountDecision(
            @NotNull(message = "aprovador e obrigatorio")
            Long approverId,

            boolean approved,

            @Size(max = 500, message = "comentario deve ter ate 500 caracteres")
            String comment
    ) {
    }
}
