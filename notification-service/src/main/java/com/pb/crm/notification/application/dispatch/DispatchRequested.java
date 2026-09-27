package com.pb.crm.notification.application.dispatch;

public record DispatchRequested(Long notificationId, Long recipientId, String channel) {
}
