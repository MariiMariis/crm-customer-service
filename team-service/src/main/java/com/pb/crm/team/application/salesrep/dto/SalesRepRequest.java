package com.pb.crm.team.application.salesrep.dto;

import com.pb.crm.team.domain.salesrep.SalesRole;
import com.pb.crm.team.domain.salesrep.SalesTeam;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SalesRepRequest(
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120, message = "nome deve ter ate 120 caracteres")
        String name,

        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email invalido")
        @Size(max = 160, message = "email deve ter ate 160 caracteres")
        String email,

        @Size(max = 20, message = "telefone deve ter ate 20 caracteres")
        String phone,

        @NotNull(message = "equipe e obrigatoria")
        SalesTeam team,

        @NotNull(message = "papel e obrigatorio")
        SalesRole role,

        @DecimalMin(value = "0.00", message = "meta mensal nao pode ser negativa")
        @Digits(integer = 13, fraction = 2, message = "meta mensal deve ter ate 13 digitos inteiros e 2 decimais")
        BigDecimal monthlyQuota,

        Long managerId,

        Long version
) {
}
