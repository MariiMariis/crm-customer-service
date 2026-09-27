package com.pb.crm.sales.application.lead.dto;

import com.pb.crm.sales.domain.lead.LeadSource;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LeadRequest(
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 80, message = "nome deve ter ate 80 caracteres")
        String firstName,

        @NotBlank(message = "sobrenome e obrigatorio")
        @Size(max = 80, message = "sobrenome deve ter ate 80 caracteres")
        String lastName,

        @Email(message = "email invalido")
        @Size(max = 160, message = "email deve ter ate 160 caracteres")
        String email,

        @Size(max = 20, message = "telefone deve ter ate 20 caracteres")
        String phone,

        @NotBlank(message = "empresa e obrigatoria")
        @Size(max = 160, message = "empresa deve ter ate 160 caracteres")
        String companyName,

        @Size(max = 100, message = "cargo deve ter ate 100 caracteres")
        String jobTitle,

        @NotNull(message = "origem e obrigatoria")
        LeadSource source,

        @DecimalMin(value = "0.00", message = "valor estimado nao pode ser negativo")
        @Digits(integer = 13, fraction = 2, message = "valor estimado deve ter ate 13 digitos inteiros e 2 decimais")
        BigDecimal estimatedValue,

        @Size(max = 2000, message = "observacoes devem ter ate 2000 caracteres")
        String notes,

        Long ownerId,

        Long version
) {
}
