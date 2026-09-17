package com.pb.notification.preference.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationPreferenceRequest(
        @NotNull(message = "emailEnabled e obrigatorio") Boolean emailEnabled,
        @NotNull(message = "smsEnabled e obrigatorio") Boolean smsEnabled,
        @NotNull(message = "inAppEnabled e obrigatorio") Boolean inAppEnabled
) {
}
