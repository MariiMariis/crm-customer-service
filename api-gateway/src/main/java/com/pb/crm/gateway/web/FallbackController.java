package com.pb.crm.gateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
public class FallbackController {

    private static final Map<String, String> LABELS = Map.of(
            "team-service", "equipe comercial",
            "accounts-service", "empresas e contatos",
            "catalog-service", "catalogo de produtos",
            "sales-service", "vendas",
            "notification-service", "notificacoes"
    );

    public record ApiError(Instant timestamp, int status, String error, String message, List<String> details) {
    }

    @RequestMapping("/fallback/{service}")
    public ResponseEntity<ApiError> fallback(@PathVariable String service) {
        String label = LABELS.getOrDefault(service, service);
        ApiError body = new ApiError(Instant.now(), HttpStatus.SERVICE_UNAVAILABLE.value(), "Service Unavailable",
                "o servico de %s esta indisponivel no momento. Tente novamente em instantes".formatted(label),
                List.of(service));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
