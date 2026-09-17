package com.pb.crm.agent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AgentRequest(
        @NotBlank(message = "nome e obrigatorio")
        String name,

        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email invalido")
        String email,

        String department,

        boolean active
) {
}
