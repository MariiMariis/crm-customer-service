package com.pb.crm.ticket.dto;

import com.pb.crm.ticket.Interaction;

import java.time.Instant;

public record InteractionResponse(
        Long id,
        String author,
        String message,
        Instant createdAt
) {
    public static InteractionResponse fromEntity(Interaction interaction) {
        return new InteractionResponse(
                interaction.getId(),
                interaction.getAuthor(),
                interaction.getMessage(),
                interaction.getCreatedAt()
        );
    }
}
