package com.pb.crm.accounts.application.company.dto;

import com.pb.crm.accounts.domain.company.BrazilianState;
import com.pb.crm.accounts.domain.company.CompanySize;
import com.pb.crm.accounts.domain.company.CompanyType;
import com.pb.crm.accounts.domain.company.Industry;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CompanyRequest(
        @NotBlank(message = "razao social e obrigatoria")
        @Size(max = 160, message = "razao social deve ter ate 160 caracteres")
        String legalName,

        @Size(max = 120, message = "nome fantasia deve ter ate 120 caracteres")
        String tradeName,

        @NotBlank(message = "CNPJ e obrigatorio")
        @Size(max = 18, message = "CNPJ deve ter ate 18 caracteres")
        String cnpj,

        @NotNull(message = "segmento e obrigatorio")
        Industry industry,

        @NotNull(message = "porte e obrigatorio")
        CompanySize size,

        @Min(value = 0, message = "numero de funcionarios nao pode ser negativo")
        Integer employees,

        @DecimalMin(value = "0.00", message = "faturamento anual nao pode ser negativo")
        @Digits(integer = 15, fraction = 2, message = "faturamento anual deve ter ate 15 digitos inteiros e 2 decimais")
        BigDecimal annualRevenue,

        @Size(max = 200, message = "site deve ter ate 200 caracteres")
        String website,

        @Size(max = 20, message = "telefone deve ter ate 20 caracteres")
        String phone,

        @Size(max = 100, message = "cidade deve ter ate 100 caracteres")
        String city,

        BrazilianState state,

        @NotNull(message = "tipo de relacionamento e obrigatorio")
        CompanyType type,

        Long ownerId,

        @Size(max = 2000, message = "observacoes devem ter ate 2000 caracteres")
        String notes,

        Long version
) {
}
