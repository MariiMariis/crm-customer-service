package com.pb.crm.notification.application.inbox;

import com.pb.crm.notification.domain.notification.Notification;
import com.pb.crm.notification.domain.notification.NotificationChannel;
import com.pb.crm.notification.domain.notification.NotificationStatus;
import com.pb.crm.notification.domain.notification.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        Long id,
        Long recipientId,
        String recipientName,
        String recipientEmail,
        NotificationChannel channel,
        NotificationType type,
        String title,
        String message,
        String link,
        NotificationStatus status,
        String skipReason,
        boolean unread,
        UUID sourceEventId,
        Instant createdAt,
        Instant sentAt,
        Instant readAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientId(),
                notification.getRecipientName(),
                notification.getRecipientEmail(),
                notification.getChannel(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getLink(),
                notification.getStatus(),
                notification.getSkipReason(),
                notification.isUnread(),
                notification.getSourceEventId(),
                notification.getAudit().createdAt(),
                notification.getSentAt(),
                notification.getReadAt()
        );
    }
}
