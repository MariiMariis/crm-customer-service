package com.pb.crm.agent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgentRequest(
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120, message = "nome deve ter ate 120 caracteres")
        String name,

        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email invalido")
        @Size(max = 160, message = "email deve ter ate 160 caracteres")
        String email,

        @Size(max = 80, message = "departamento deve ter ate 80 caracteres")
        String department,

        boolean active,

        Long version
) {
}
