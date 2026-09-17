package com.pb.crm.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120, message = "nome deve ter ate 120 caracteres")
        String name,

        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email invalido")
        @Size(max = 160, message = "email deve ter ate 160 caracteres")
        String email,

        @Size(max = 20, message = "telefone deve ter ate 20 caracteres")
        String phone,

        @Size(max = 20, message = "documento deve ter ate 20 caracteres")
        String document,

        Long version
) {
}
