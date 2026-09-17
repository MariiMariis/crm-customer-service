package com.pb.crm.notification;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableFeignClients(basePackageClasses = NotificationClient.class)
@EnableConfigurationProperties(NotificationProperties.class)
public class NotificationClientConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> notificationCircuitBreakerCustomizer(NotificationProperties properties) {
        NotificationProperties.CircuitBreaker settings = properties.circuitBreaker();
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(settings.slidingWindowSize())
                .minimumNumberOfCalls(settings.minimumNumberOfCalls())
                .failureRateThreshold(settings.failureRateThreshold())
                .waitDurationInOpenState(Duration.ofSeconds(settings.waitDurationInOpenStateSeconds()))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(settings.timeoutSeconds()))
                .build();
        return factory -> factory.configure(builder -> builder
                        .circuitBreakerConfig(circuitBreakerConfig)
                        .timeLimiterConfig(timeLimiterConfig),
                NotificationGateway.CIRCUIT_BREAKER_ID);
    }
}
