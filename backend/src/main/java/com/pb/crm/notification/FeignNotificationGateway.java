package com.pb.crm.notification;

import com.pb.crm.notification.dto.NotificationPreferenceRequest;
import com.pb.crm.notification.dto.NotificationPreferenceResponse;
import com.pb.crm.notification.dto.NotificationRequest;
import com.pb.crm.notification.dto.NotificationResponse;
import com.pb.crm.notification.dto.NotificationServiceStatus;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class FeignNotificationGateway implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(FeignNotificationGateway.class);

    private final NotificationClient client;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final DiscoveryClient discoveryClient;
    private final NotificationProperties properties;

    public FeignNotificationGateway(NotificationClient client,
                                    CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                                    CircuitBreakerRegistry circuitBreakerRegistry,
                                    DiscoveryClient discoveryClient,
                                    NotificationProperties properties) {
        this.client = client;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.discoveryClient = discoveryClient;
        this.properties = properties;
    }

    @Override
    public Optional<NotificationResponse> send(NotificationRequest request) {
        if (!properties.enabled()) {
            log.debug("Integracao com o servico de notificacoes desabilitada; notificacao {} do ticket {} ignorada",
                    request.type(), request.ticketId());
            return Optional.empty();
        }
        return circuitBreakerFactory.create(CIRCUIT_BREAKER_ID).run(
                () -> Optional.of(client.create(request)),
                throwable -> {
                    log.warn("Nao foi possivel enviar a notificacao {} do ticket {} ao servico de notificacoes: {}",
                            request.type(), request.ticketId(), rootMessage(throwable));
                    return Optional.empty();
                });
    }

    @Override
    public List<NotificationResponse> findByTicket(Long ticketId) {
        return call(() -> client.findByTicket(ticketId));
    }

    @Override
    public NotificationPreferenceResponse findPreferences(Long customerId) {
        return call(() -> client.findPreferences(customerId));
    }

    @Override
    public NotificationPreferenceResponse updatePreferences(Long customerId, NotificationPreferenceRequest request) {
        return call(() -> client.updatePreferences(customerId, request));
    }

    @Override
    public NotificationServiceStatus status() {
        List<String> instances = discoveryClient.getInstances(NotificationClient.SERVICE_ID).stream()
                .map(ServiceInstance::getUri)
                .map(Object::toString)
                .toList();
        String circuitState = circuitBreakerRegistry.find(CIRCUIT_BREAKER_ID)
                .map(cb -> cb.getState().name())
                .orElse("CLOSED");
        if (!properties.enabled()) {
            return new NotificationServiceStatus(false, false, "DISABLED", circuitState, instances);
        }
        try {
            Map<String, Object> health = call(client::health);
            String status = String.valueOf(health.getOrDefault("status", "UNKNOWN"));
            return new NotificationServiceStatus(true, "UP".equals(status), status, circuitState, instances);
        } catch (NotificationServiceUnavailableException ex) {
            return new NotificationServiceStatus(true, false, "DOWN", circuitState, instances);
        }
    }

    private <T> T call(Supplier<T> supplier) {
        if (!properties.enabled()) {
            throw new NotificationServiceUnavailableException("a integracao com o servico de notificacoes esta desabilitada");
        }
        return circuitBreakerFactory.create(CIRCUIT_BREAKER_ID).run(supplier, throwable -> {
            log.warn("Chamada ao servico de notificacoes falhou: {}", rootMessage(throwable));
            throw new NotificationServiceUnavailableException(throwable);
        });
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getClass().getSimpleName() + ": " + current.getMessage();
    }
}
