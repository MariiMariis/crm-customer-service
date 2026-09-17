package com.pb.crm.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.notifications")
public record NotificationProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue CircuitBreaker circuitBreaker
) {
    public record CircuitBreaker(
            @DefaultValue("6") int slidingWindowSize,
            @DefaultValue("3") int minimumNumberOfCalls,
            @DefaultValue("50") float failureRateThreshold,
            @DefaultValue("15") long waitDurationInOpenStateSeconds,
            @DefaultValue("3") long timeoutSeconds
    ) {
    }
}
