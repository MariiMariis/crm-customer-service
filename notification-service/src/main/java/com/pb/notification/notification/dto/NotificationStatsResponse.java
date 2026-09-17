package com.pb.notification.notification.dto;

import com.pb.notification.notification.NotificationChannel;
import com.pb.notification.notification.NotificationStatus;

import java.util.Map;

public record NotificationStatsResponse(
        long total,
        long unread,
        Map<NotificationStatus, Long> byStatus,
        Map<NotificationChannel, Long> byChannel
) {
}
