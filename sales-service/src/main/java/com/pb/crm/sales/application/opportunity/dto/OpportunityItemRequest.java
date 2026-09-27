package com.pb.crm.sales.application.opportunity.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OpportunityItemRequest(
        @NotNull(message = "produto e obrigatorio")
        Long productId,

        @Min(value = 1, message = "quantidade deve ser maior que zero")
        int quantity,

        @DecimalMin(value = "0.00", message = "desconto nao pode ser negativo")
        @DecimalMax(value = "100.00", message = "desconto nao pode passar de 100%")
        BigDecimal discountPercent
) {
}
