package com.pb.crm.catalog.application.product.dto;

import com.pb.crm.catalog.domain.product.BillingType;
import com.pb.crm.catalog.domain.product.ProductSubcategory;
import com.pb.crm.catalog.domain.product.UnitOfMeasure;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 160, message = "nome deve ter ate 160 caracteres")
        String name,

        @Size(max = 2000, message = "descricao deve ter ate 2000 caracteres")
        String description,

        @NotNull(message = "subcategoria e obrigatoria")
        ProductSubcategory subcategory,

        @NotNull(message = "tipo de cobranca e obrigatorio")
        BillingType billing,

        @NotNull(message = "unidade de medida e obrigatoria")
        UnitOfMeasure unit,

        @NotNull(message = "preco de lista e obrigatorio")
        @DecimalMin(value = "0.01", message = "preco de lista deve ser maior que zero")
        @Digits(integer = 13, fraction = 2, message = "preco de lista deve ter ate 13 digitos inteiros e 2 decimais")
        BigDecimal unitPrice,

        @DecimalMin(value = "0.00", message = "custo nao pode ser negativo")
        @Digits(integer = 13, fraction = 2, message = "custo deve ter ate 13 digitos inteiros e 2 decimais")
        BigDecimal unitCost,

        @NotNull(message = "desconto maximo e obrigatorio")
        @DecimalMin(value = "0.00", message = "desconto maximo nao pode ser negativo")
        @DecimalMax(value = "100.00", message = "desconto maximo nao pode passar de 100%")
        @Digits(integer = 3, fraction = 2, message = "desconto maximo deve ter ate 2 casas decimais")
        BigDecimal maxDiscountPercent,

        @Size(max = 80, message = "fabricante deve ter ate 80 caracteres")
        String manufacturer,

        @Min(value = 0, message = "garantia nao pode ser negativa")
        @Max(value = 120, message = "garantia deve ser de ate 120 meses")
        Integer warrantyMonths,

        Long version
) {
}
