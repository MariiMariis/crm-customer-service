package com.pb.crm.notification.dto;

import java.util.List;

public record NotificationServiceStatus(
        boolean enabled,
        boolean available,
        String health,
        String circuitState,
        List<String> instances
) {
}
