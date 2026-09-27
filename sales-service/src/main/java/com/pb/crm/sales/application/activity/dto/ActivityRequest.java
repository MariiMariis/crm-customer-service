package com.pb.crm.sales.application.activity.dto;

import com.pb.crm.sales.domain.activity.ActivityPriority;
import com.pb.crm.sales.domain.activity.ActivityType;
import com.pb.crm.sales.domain.activity.RelatedType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ActivityRequest(
        @NotNull(message = "tipo e obrigatorio")
        ActivityType type,

        @NotBlank(message = "assunto e obrigatorio")
        @Size(max = 160, message = "assunto deve ter ate 160 caracteres")
        String subject,

        @Size(max = 2000, message = "descricao deve ter ate 2000 caracteres")
        String description,

        ActivityPriority priority,

        @NotNull(message = "tipo do registro relacionado e obrigatorio")
        RelatedType relatedType,

        @NotNull(message = "registro relacionado e obrigatorio")
        Long relatedId,

        @NotNull(message = "responsavel e obrigatorio")
        Long ownerId,

        Instant dueAt,

        Instant startsAt,

        Instant endsAt,

        @Size(max = 300, message = "local deve ter ate 300 caracteres")
        String location,

        Long version
) {
}
