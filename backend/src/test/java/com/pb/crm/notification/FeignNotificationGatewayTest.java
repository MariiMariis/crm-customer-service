package com.pb.crm.notification;

import com.pb.crm.notification.dto.NotificationPreferenceRequest;
import com.pb.crm.notification.dto.NotificationPreferenceResponse;
import com.pb.crm.notification.dto.NotificationRequest;
import com.pb.crm.notification.dto.NotificationResponse;
import com.pb.crm.notification.dto.NotificationServiceStatus;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.net.ConnectException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:crmtest-gateway;DB_CLOSE_DELAY=-1")
class FeignNotificationGatewayTest {

    @Autowired
    private NotificationGateway gateway;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private NotificationClient client;

    private static NotificationRequest request() {
        return new NotificationRequest(1L, 2L, "Ana", "ana@example.com", null,
                NotificationChannel.EMAIL, NotificationType.TICKET_CREATED, "Assunto", "Mensagem", "maria");
    }

    private static NotificationResponse response(Long id) {
        return new NotificationResponse(id, 1L, 2L, "Ana", "ana@example.com", null,
                NotificationChannel.EMAIL, NotificationType.TICKET_CREATED, "PENDING", "Assunto", "Mensagem",
                null, "maria", 0, Instant.now(), null, null);
    }

    @BeforeEach
    void resetCircuit() {
        reset(client);
        circuitBreakerRegistry.find(NotificationGateway.CIRCUIT_BREAKER_ID).ifPresent(CircuitBreaker::reset);
    }

    @Test
    void deveRepassarChamadasAoClienteFeignQuandoServicoResponde() {
        when(client.create(any())).thenReturn(response(10L));
        when(client.findByTicket(1L)).thenReturn(List.of(response(10L)));
        when(client.findPreferences(2L)).thenReturn(new NotificationPreferenceResponse(2L, true, false, true, true, Instant.now()));
        when(client.updatePreferences(eq(2L), any())).thenReturn(new NotificationPreferenceResponse(2L, false, false, true, true, Instant.now()));
        when(client.health()).thenReturn(Map.of("status", "UP"));

        Optional<NotificationResponse> sent = gateway.send(request());
        assertThat(sent).isPresent();
        assertThat(sent.get().id()).isEqualTo(10L);
        assertThat(gateway.findByTicket(1L)).hasSize(1);
        assertThat(gateway.findPreferences(2L).smsEnabled()).isFalse();
        assertThat(gateway.updatePreferences(2L, new NotificationPreferenceRequest(false, false, true)).emailEnabled()).isFalse();

        NotificationServiceStatus status = gateway.status();
        assertThat(status.enabled()).isTrue();
        assertThat(status.available()).isTrue();
        assertThat(status.health()).isEqualTo("UP");
        assertThat(status.circuitState()).isEqualTo("CLOSED");
        assertThat(status.instances()).containsExactly("http://localhost:8081");
    }

    @Test
    void envioDeveDegradarSilenciosamenteQuandoServicoFalha() {
        when(client.create(any())).thenThrow(new RuntimeException(new ConnectException("Connection refused")));

        assertThat(gateway.send(request())).isEmpty();
        verify(client, times(1)).create(any());
    }

    @Test
    void consultasDevemLancarIndisponibilidadeQuandoServicoFalha() {
        when(client.findByTicket(anyLong())).thenThrow(new RuntimeException(new ConnectException("Connection refused")));
        when(client.health()).thenThrow(new RuntimeException(new ConnectException("Connection refused")));

        assertThatThrownBy(() -> gateway.findByTicket(1L))
                .isInstanceOf(NotificationServiceUnavailableException.class)
                .hasMessageContaining("indisponivel");

        NotificationServiceStatus status = gateway.status();
        assertThat(status.available()).isFalse();
        assertThat(status.health()).isEqualTo("DOWN");
    }

    @Test
    void circuitBreakerDeveAbrirAposFalhasConsecutivasEDeixarDeChamarOServico() {
        when(client.findByTicket(anyLong())).thenThrow(new RuntimeException(new ConnectException("Connection refused")));

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> gateway.findByTicket(1L)).isInstanceOf(NotificationServiceUnavailableException.class);
        }

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(NotificationGateway.CIRCUIT_BREAKER_ID);
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        assertThatThrownBy(() -> gateway.findByTicket(1L)).isInstanceOf(NotificationServiceUnavailableException.class);
        verify(client, times(3)).findByTicket(anyLong());
        assertThat(gateway.status().circuitState()).isEqualTo("OPEN");
    }
}
