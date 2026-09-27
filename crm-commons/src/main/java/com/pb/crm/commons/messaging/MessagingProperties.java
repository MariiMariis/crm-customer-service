package com.pb.crm.commons.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "crm.messaging")
public record MessagingProperties(
        @DefaultValue("true") boolean enabled,
        String source,
        String exchange,
        @DefaultValue Outbox outbox,
        @DefaultValue Retry retry
) {
    public record Outbox(
            @DefaultValue("true") boolean relayEnabled,
            @DefaultValue("500ms") Duration pollInterval,
            @DefaultValue("50") int batchSize,
            @DefaultValue("5s") Duration confirmTimeout
    ) {
    }

    public record Retry(
            @DefaultValue({"5s", "30s", "2m"}) List<Duration> delays
    ) {
    }
}
