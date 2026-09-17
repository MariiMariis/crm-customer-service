package com.pb.notification.notification.dto;

import com.pb.notification.notification.Notification;
import com.pb.notification.notification.NotificationChannel;
import com.pb.notification.notification.NotificationStatus;
import com.pb.notification.notification.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long ticketId,
        Long customerId,
        String recipientName,
        String recipientEmail,
        String recipientPhone,
        NotificationChannel channel,
        NotificationType type,
        NotificationStatus status,
        String subject,
        String message,
        String failureReason,
        String requestedBy,
        int attempts,
        Instant createdAt,
        Instant sentAt,
        Instant readAt
) {
    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTicketId(),
                notification.getCustomerId(),
                notification.getRecipientName(),
                notification.getRecipientEmail(),
                notification.getRecipientPhone(),
                notification.getChannel(),
                notification.getType(),
                notification.getStatus(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getFailureReason(),
                notification.getRequestedBy(),
                notification.getAttempts(),
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getReadAt()
        );
    }
}
