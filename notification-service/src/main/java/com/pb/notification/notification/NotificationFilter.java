package com.pb.notification.notification;

public record NotificationFilter(
        Long ticketId,
        Long customerId,
        NotificationStatus status,
        NotificationChannel channel,
        NotificationType type
) {
}
