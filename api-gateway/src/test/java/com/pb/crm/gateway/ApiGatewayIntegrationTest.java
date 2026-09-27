package com.pb.crm.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayIntegrationTest {

    private static final String UNREACHABLE = "http://localhost:1";
    private static final List<String> RECEIVED = new CopyOnWriteArrayList<>();
    private static final HttpServer FAKE_TEAM_SERVICE = startFakeTeamService();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static HttpServer startFakeTeamService() {
        ApiGatewayApplication.configureWindowsLoopbackTempDir();
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", exchange -> {
                RECEIVED.add(exchange.getRequestMethod() + " " + exchange.getRequestURI() + " actor="
                        + exchange.getRequestHeaders().getFirst("X-Actor"));
                String path = exchange.getRequestURI().getPath();
                String body = switch (path) {
                    case "/actuator/health" -> "{\"status\":\"UP\",\"components\":{\"db\":{\"status\":\"UP\"},\"rabbit\":{\"status\":\"UP\"}}}";
                    case "/api/messaging/status" -> "{\"outbox\":{\"pending\":3},\"queues\":[{\"name\":\"team.q\",\"messages\":0,\"consumers\":1,\"retrying\":0,\"deadLetters\":2}]}";
                    default -> "{\"content\":[{\"id\":1,\"name\":\"Ana\"}],\"totalElements\":1}";
                };
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(bytes);
                }
            });
            server.start();
            return server;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @DynamicPropertySource
    static void services(DynamicPropertyRegistry registry) {
        registry.add("crm.gateway.services.team-service",
                () -> "http://localhost:" + FAKE_TEAM_SERVICE.getAddress().getPort());
        registry.add("crm.gateway.services.accounts-service", () -> UNREACHABLE);
        registry.add("crm.gateway.services.catalog-service", () -> UNREACHABLE);
        registry.add("crm.gateway.services.sales-service", () -> UNREACHABLE);
        registry.add("crm.gateway.services.notification-service", () -> UNREACHABLE);
        registry.add("crm.gateway.config-server-url", () -> UNREACHABLE);
        registry.add("crm.gateway.health-timeout", () -> "500ms");
    }

    @AfterAll
    static void stopFakeService() {
        FAKE_TEAM_SERVICE.stop(0);
    }

    @Test
    void routesRequestsToTheOwningServiceKeepingQueryAndActorHeader() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Actor", "gestora.ana");
        ResponseEntity<String> response = restTemplate.exchange("/api/sales-reps?team=FIELD_SALES", HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(response.getBody()).get("totalElements").asInt()).isEqualTo(1);
        assertThat(RECEIVED).anyMatch(line -> line.equals("GET /api/sales-reps?team=FIELD_SALES actor=gestora.ana"));
    }

    @Test
    void platformMessagingPathIsRewrittenToTheServiceMessagingApi() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/platform/team/messaging/status", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(RECEIVED).anyMatch(line -> line.startsWith("GET /api/messaging/status"));
    }

    @Test
    void unavailableServiceAnswersWithFriendlyFallback() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/leads", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("message").asText()).contains("vendas");
        assertThat(body.get("details").get(0).asText()).isEqualTo("sales-service");
    }

    @Test
    void corsPreflightIsAnsweredForTheFrontend() {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("http://localhost:3000");
        headers.setAccessControlRequestMethod(HttpMethod.POST);
        headers.setAccessControlRequestHeaders(List.of("Content-Type", "X-Actor"));
        ResponseEntity<String> response = restTemplate.exchange("/api/companies", HttpMethod.OPTIONS,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getAccessControlAllowOrigin()).isEqualTo("http://localhost:3000");

        HttpHeaders foreign = new HttpHeaders();
        foreign.setOrigin("http://malicioso.example");
        foreign.setAccessControlRequestMethod(HttpMethod.GET);
        assertThat(restTemplate.exchange("/api/companies", HttpMethod.OPTIONS, new HttpEntity<>(foreign), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void platformHealthAggregatesServicesAndMessaging() throws Exception {
        JsonNode health = objectMapper.readTree(restTemplate.getForObject("/api/platform/health", String.class));

        assertThat(health.get("status").asText()).isEqualTo("DEGRADED");
        assertThat(health.get("services")).hasSize(6);
        JsonNode team = health.get("services").get(0);
        assertThat(team.get("name").asText()).isEqualTo("team-service");
        assertThat(team.get("status").asText()).isEqualTo("UP");
        assertThat(team.get("components").get("rabbit").asText()).isEqualTo("UP");
        assertThat(team.get("messaging").get("outboxPending").asLong()).isEqualTo(3);
        assertThat(team.get("messaging").get("deadLetters").asLong()).isEqualTo(2);
        assertThat(health.get("services").get(3).get("status").asText()).isEqualTo("DOWN");
    }
}
