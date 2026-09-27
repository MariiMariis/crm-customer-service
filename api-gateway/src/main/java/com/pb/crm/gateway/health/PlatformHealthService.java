package com.pb.crm.gateway.health;

import com.fasterxml.jackson.databind.JsonNode;
import com.pb.crm.gateway.GatewayProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PlatformHealthService {

    public record Subscription(String exchange, String routingKey) {
    }

    public record QueueHealth(String name, Long messages, Integer consumers, long retrying, long deadLetters,
                              List<Subscription> subscriptions) {
    }

    public record MessagingHealth(String exchange, long outboxPending, long published, long deadLetters,
                                  List<QueueHealth> queues) {
    }

    public record ServiceHealth(String name, String url, String status, long latencyMs,
                                Map<String, String> components, MessagingHealth messaging) {
    }

    public record PlatformHealth(String status, Instant checkedAt, List<ServiceHealth> services) {
    }

    private final GatewayProperties properties;
    private final RestClient restClient;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public PlatformHealthService(GatewayProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.healthTimeout());
        requestFactory.setReadTimeout(properties.healthTimeout());
        this.restClient = builder.requestFactory(requestFactory).build();
    }

    public PlatformHealth check() {
        Map<String, String> targets = new LinkedHashMap<>(properties.services());
        if (properties.configServerUrl() != null) {
            targets.put("config-server", properties.configServerUrl());
        }
        List<CompletableFuture<ServiceHealth>> futures = targets.entrySet().stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> checkService(entry.getKey(), entry.getValue()), executor))
                .toList();
        List<ServiceHealth> services = futures.stream().map(CompletableFuture::join).toList();
        long down = services.stream().filter(service -> !"UP".equals(service.status())).count();
        String overall = down == 0 ? "UP" : down == services.size() ? "DOWN" : "DEGRADED";
        return new PlatformHealth(overall, Instant.now(), services);
    }

    private ServiceHealth checkService(String name, String url) {
        long start = System.nanoTime();
        JsonNode health = get(url + "/actuator/health");
        long latency = (System.nanoTime() - start) / 1_000_000;
        if (health == null) {
            return new ServiceHealth(name, url, "DOWN", latency, Map.of(), null);
        }
        Map<String, String> components = new LinkedHashMap<>();
        health.path("components").fields().forEachRemaining(component -> {
            if (List.of("db", "rabbit", "diskSpace").contains(component.getKey())) {
                components.put(component.getKey(), component.getValue().path("status").asText());
            }
        });
        MessagingHealth messaging = "config-server".equals(name) ? null : messaging(get(url + "/api/messaging/status"));
        return new ServiceHealth(name, url, health.path("status").asText("UNKNOWN"), latency, components, messaging);
    }

    private MessagingHealth messaging(JsonNode status) {
        if (status == null) {
            return null;
        }
        List<QueueHealth> queues = new ArrayList<>();
        long deadLetters = 0;
        for (JsonNode queue : status.path("queues")) {
            long dlq = queue.path("deadLetters").asLong();
            deadLetters += dlq;
            List<Subscription> subscriptions = new ArrayList<>();
            for (JsonNode subscription : queue.path("subscriptions")) {
                subscriptions.add(new Subscription(subscription.path("exchange").asText(), subscription.path("routingKey").asText()));
            }
            queues.add(new QueueHealth(
                    queue.path("name").asText(),
                    queue.path("messages").isNull() ? null : queue.path("messages").asLong(),
                    queue.path("consumers").isNull() ? null : queue.path("consumers").asInt(),
                    queue.path("retrying").asLong(),
                    dlq,
                    subscriptions));
        }
        return new MessagingHealth(
                status.path("exchange").asText(null),
                status.path("outbox").path("pending").asLong(),
                status.path("outbox").path("published").asLong(),
                deadLetters,
                queues);
    }

    private JsonNode get(String url) {
        try {
            return restClient.get().uri(url).retrieve().body(JsonNode.class);
        } catch (RestClientResponseException ex) {
            try {
                return ex.getResponseBodyAs(JsonNode.class);
            } catch (RuntimeException ignored) {
                return null;
            }
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
