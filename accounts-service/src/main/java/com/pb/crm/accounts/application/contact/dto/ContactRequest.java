package com.pb.crm.accounts.application.contact.dto;

import com.pb.crm.accounts.domain.contact.DecisionRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotNull(message = "empresa e obrigatoria")
        Long companyId,

        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 80, message = "nome deve ter ate 80 caracteres")
        String firstName,

        @NotBlank(message = "sobrenome e obrigatorio")
        @Size(max = 80, message = "sobrenome deve ter ate 80 caracteres")
        String lastName,

        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email invalido")
        @Size(max = 160, message = "email deve ter ate 160 caracteres")
        String email,

        @Size(max = 20, message = "telefone deve ter ate 20 caracteres")
        String phone,

        @Size(max = 20, message = "celular deve ter ate 20 caracteres")
        String mobile,

        @Size(max = 100, message = "cargo deve ter ate 100 caracteres")
        String jobTitle,

        @Size(max = 80, message = "departamento deve ter ate 80 caracteres")
        String department,

        @NotNull(message = "papel na decisao e obrigatorio")
        DecisionRole decisionRole,

        Boolean primary,

        Boolean active,

        Long version
) {
}
