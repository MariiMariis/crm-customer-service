package com.pb.crm.sales.application.lead.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ConvertLeadRequest(
        @NotBlank(message = "CNPJ e obrigatorio")
        @Size(max = 18, message = "CNPJ deve ter ate 18 caracteres")
        String cnpj,

        @NotBlank(message = "segmento e obrigatorio")
        String industry,

        @NotBlank(message = "porte e obrigatorio")
        String companySize,

        @Size(max = 100, message = "cidade deve ter ate 100 caracteres")
        String city,

        @Size(min = 2, max = 2, message = "UF deve ter 2 letras")
        String state,

        boolean createOpportunity,

        @Size(max = 160, message = "titulo da oportunidade deve ter ate 160 caracteres")
        String opportunityTitle,

        @FutureOrPresent(message = "data prevista de fechamento nao pode estar no passado")
        LocalDate expectedCloseDate
) {
}
