package com.pb.crm.notification.dto;

import java.time.Instant;

public record NotificationPreferenceResponse(
        Long customerId,
        boolean emailEnabled,
        boolean smsEnabled,
        boolean inAppEnabled,
        boolean persisted,
        Instant updatedAt
) {
}
