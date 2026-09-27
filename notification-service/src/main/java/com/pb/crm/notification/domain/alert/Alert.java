package com.pb.crm.notification.domain.alert;

import com.pb.crm.notification.domain.notification.NotificationType;

import java.util.List;

public record Alert(
        NotificationType type,
        List<RecipientRole> recipients,
        String title,
        String message,
        String link
) {
}
