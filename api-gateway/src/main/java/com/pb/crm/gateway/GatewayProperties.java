package com.pb.crm.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "crm.gateway")
public record GatewayProperties(
        Map<String, String> services,
        String configServerUrl,
        List<String> allowedOrigins,
        @DefaultValue("2s") Duration healthTimeout,
        @DefaultValue CircuitBreaker circuitBreaker
) {
    public record CircuitBreaker(
            @DefaultValue("10s") Duration timeout,
            @DefaultValue("50") float failureRateThreshold,
            @DefaultValue("10") int slidingWindowSize,
            @DefaultValue("5") int minimumNumberOfCalls,
            @DefaultValue("15s") Duration waitDurationInOpenState
    ) {
    }
}
