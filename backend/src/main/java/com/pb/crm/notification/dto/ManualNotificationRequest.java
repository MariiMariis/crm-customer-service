package com.pb.crm.notification.dto;

import com.pb.crm.notification.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ManualNotificationRequest(
        @NotNull(message = "channel e obrigatorio") NotificationChannel channel,
        @NotBlank(message = "subject e obrigatorio") @Size(max = 160) String subject,
        @NotBlank(message = "message e obrigatoria") @Size(max = 2000) String message
) {
}
