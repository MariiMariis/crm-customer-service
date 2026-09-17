package com.pb.crm.notification.dto;

import com.pb.crm.notification.NotificationChannel;
import com.pb.crm.notification.NotificationType;

public record NotificationRequest(
        Long ticketId,
        Long customerId,
        String recipientName,
        String recipientEmail,
        String recipientPhone,
        NotificationChannel channel,
        NotificationType type,
        String subject,
        String message,
        String requestedBy
) {
}
