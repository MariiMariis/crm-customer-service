package com.pb.crm.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload de entrada para criar/atualizar um cliente.
 */
public record CustomerRequest(
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120, message = "nome deve ter ate 120 caracteres")
        String name,

        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email invalido")
        String email,

        String phone,

        String document
) {
}
