package com.pb.crm.notification.domain.preference;

import com.pb.crm.notification.domain.notification.NotificationChannel;

import java.time.Instant;

public record NotificationPreference(
        Long salesRepId,
        boolean emailEnabled,
        boolean persisted,
        Instant updatedAt
) {
    public static NotificationPreference defaults(Long salesRepId) {
        return new NotificationPreference(salesRepId, true, false, null);
    }

    public boolean allows(NotificationChannel channel) {
        return channel == NotificationChannel.IN_APP || emailEnabled;
    }

    public NotificationPreference withEmail(boolean enabled) {
        return new NotificationPreference(salesRepId, enabled, true, Instant.now());
    }
}
