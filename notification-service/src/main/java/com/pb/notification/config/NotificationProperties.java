package com.pb.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.notifications")
public record NotificationProperties(
        @DefaultValue("PB CRM Customer Service") String senderName,
        @DefaultValue("no-reply@pbcrm.local") String senderEmail,
        @DefaultValue("3") int maxAttempts,
        @DefaultValue Dispatch dispatch
) {
    public record Dispatch(
            @DefaultValue("true") boolean auto,
            @DefaultValue("5000") long fixedDelay,
            @DefaultValue("20") int batchSize
    ) {
    }
}
