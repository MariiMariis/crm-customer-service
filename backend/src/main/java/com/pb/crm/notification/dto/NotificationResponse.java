package com.pb.crm.notification.dto;

import com.pb.crm.notification.NotificationChannel;
import com.pb.crm.notification.NotificationType;

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
        String status,
        String subject,
        String message,
        String failureReason,
        String requestedBy,
        int attempts,
        Instant createdAt,
        Instant sentAt,
        Instant readAt
) {
}
