package com.pb.notification.notification.dto;

import com.pb.notification.notification.NotificationChannel;
import com.pb.notification.notification.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
        @NotNull(message = "ticketId e obrigatorio") Long ticketId,
        @NotNull(message = "customerId e obrigatorio") Long customerId,
        @NotBlank(message = "recipientName e obrigatorio") @Size(max = 120) String recipientName,
        @Email(message = "recipientEmail invalido") @Size(max = 160) String recipientEmail,
        @Size(max = 20) String recipientPhone,
        @NotNull(message = "channel e obrigatorio") NotificationChannel channel,
        @NotNull(message = "type e obrigatorio") NotificationType type,
        @NotBlank(message = "subject e obrigatorio") @Size(max = 160) String subject,
        @NotBlank(message = "message e obrigatoria") @Size(max = 2000) String message,
        @Size(max = 120) String requestedBy
) {
}
