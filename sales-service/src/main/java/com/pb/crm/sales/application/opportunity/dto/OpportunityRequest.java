package com.pb.crm.sales.application.opportunity.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OpportunityRequest(
        @NotBlank(message = "titulo e obrigatorio")
        @Size(max = 160, message = "titulo deve ter ate 160 caracteres")
        String title,

        @Size(max = 2000, message = "descricao deve ter ate 2000 caracteres")
        String description,

        @NotNull(message = "empresa e obrigatoria")
        Long companyId,

        Long contactId,

        @NotNull(message = "vendedor responsavel e obrigatorio")
        Long ownerId,

        @NotNull(message = "data prevista de fechamento e obrigatoria")
        LocalDate expectedCloseDate,

        @Min(value = 1, message = "prazo do contrato deve ser de ao menos 1 mes")
        @Max(value = 60, message = "prazo do contrato deve ser de ate 60 meses")
        Integer contractTermMonths,

        @DecimalMin(value = "0.00", message = "valor estimado nao pode ser negativo")
        @Digits(integer = 15, fraction = 2, message = "valor estimado deve ter ate 15 digitos inteiros e 2 decimais")
        BigDecimal estimatedValue,

        Long version
) {
    public int termOrDefault() {
        return contractTermMonths == null ? 12 : contractTermMonths;
    }
}
