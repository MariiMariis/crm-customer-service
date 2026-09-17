package com.pb.notification.preference.dto;

import com.pb.notification.preference.NotificationPreference;

import java.time.Instant;

public record NotificationPreferenceResponse(
        Long customerId,
        boolean emailEnabled,
        boolean smsEnabled,
        boolean inAppEnabled,
        boolean persisted,
        Instant updatedAt
) {
    public static NotificationPreferenceResponse fromEntity(NotificationPreference preference) {
        return new NotificationPreferenceResponse(
                preference.getCustomerId(),
                preference.isEmailEnabled(),
                preference.isSmsEnabled(),
                preference.isInAppEnabled(),
                true,
                preference.getUpdatedAt()
        );
    }

    public static NotificationPreferenceResponse defaults(Long customerId) {
        return new NotificationPreferenceResponse(customerId, true, true, true, false, null);
    }
}
