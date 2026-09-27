package com.pb.crm.notification.domain.notification;

public record NotificationCriteria(
        Long recipientId,
        NotificationChannel channel,
        NotificationStatus status,
        NotificationType type,
        boolean unreadOnly
) {
}
