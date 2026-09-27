package com.pb.crm.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class CrmApiClient {

    private static final Logger log = LoggerFactory.getLogger(CrmApiClient.class);
    private static final String NOT_SYNCED_HINT = "base sincronizada";

    private final RestClient restClient;
    private final SeederProperties properties;

    public CrmApiClient(RestClient.Builder builder, SeederProperties properties) {
        this.properties = properties;
        this.restClient = builder
                .baseUrl(properties.gatewayUrl())
                .defaultHeader("X-Actor", properties.actor())
                .build();
    }

    public JsonNode get(String path, Object... uriVariables) {
        return restClient.get().uri(path, uriVariables).retrieve().body(JsonNode.class);
    }

    public JsonNode post(String path, Object body, Object... uriVariables) {
        return awaitingReplicas(() -> restClient.post()
                .uri(path, uriVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body == null ? Map.of() : body)
                .retrieve()
                .body(JsonNode.class));
    }

    public JsonNode put(String path, Object body, Object... uriVariables) {
        return restClient.put()
                .uri(path, uriVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    public long total(String path) {
        return get(path).path("totalElements").asLong();
    }

    public JsonNode waitUntil(String description, Supplier<JsonNode> probe, java.util.function.Predicate<JsonNode> condition) {
        Instant deadline = Instant.now().plus(properties.replicaWait());
        JsonNode last = null;
        while (Instant.now().isBefore(deadline)) {
            try {
                last = probe.get();
                if (last != null && condition.test(last)) {
                    return last;
                }
            } catch (RestClientResponseException ignored) {
                last = null;
            }
            pause(Duration.ofMillis(400));
        }
        throw new IllegalStateException("tempo esgotado aguardando: " + description);
    }

    private JsonNode awaitingReplicas(Supplier<JsonNode> call) {
        Instant deadline = Instant.now().plus(properties.replicaWait());
        while (true) {
            try {
                return call.get();
            } catch (RestClientResponseException ex) {
                boolean waitingReplica = isConflict(ex.getStatusCode())
                        && ex.getResponseBodyAsString().contains(NOT_SYNCED_HINT);
                if (!waitingReplica || Instant.now().isAfter(deadline)) {
                    throw new IllegalStateException("falha na chamada: %s %s".formatted(ex.getStatusCode(),
                            ex.getResponseBodyAsString()), ex);
                }
                log.debug("Aguardando replica: {}", ex.getResponseBodyAsString());
                pause(Duration.ofMillis(400));
            }
        }
    }

    private static boolean isConflict(HttpStatusCode status) {
        return status.value() == 409;
    }

    private static void pause(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("carga interrompida", ex);
        }
    }
}
