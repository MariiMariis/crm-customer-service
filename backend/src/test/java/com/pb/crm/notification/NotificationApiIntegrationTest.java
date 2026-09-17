package com.pb.crm.notification;

import com.pb.crm.notification.dto.NotificationPreferenceResponse;
import com.pb.crm.notification.dto.NotificationRequest;
import com.pb.crm.notification.dto.NotificationResponse;
import com.pb.crm.notification.dto.NotificationServiceStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:crmtest-notification;DB_CLOSE_DELAY=-1")
class NotificationApiIntegrationTest {

    private static final String JSON = "application/json";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationGateway gateway;

    private static NotificationResponse response(Long id, Long ticketId, Long customerId, NotificationType type) {
        return new NotificationResponse(id, ticketId, customerId, "Cliente", "c@example.com", null,
                NotificationChannel.EMAIL, type, "SENT", "Assunto", "Mensagem",
                null, "web", 1, Instant.now(), Instant.now(), null);
    }

    private long createCustomerAndTicket() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String customerResponse = mockMvc.perform(post("/api/customers").contentType(JSON)
                        .content("{\"name\":\"Cliente %s\",\"email\":\"cliente.%s@example.com\"}".formatted(suffix, suffix)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long customerId = extractId(customerResponse);

        String ticketResponse = mockMvc.perform(post("/api/tickets").contentType(JSON)
                        .content("{\"subject\":\"Assunto\",\"description\":\"Descricao\",\"priority\":\"LOW\",\"customerId\":%d}".formatted(customerId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extractId(ticketResponse);
    }

    @Test
    void deveListarNotificacoesDoTicketViaMicrosservico() throws Exception {
        long ticketId = createCustomerAndTicket();
        when(gateway.findByTicket(ticketId)).thenReturn(List.of(
                response(1L, ticketId, 5L, NotificationType.TICKET_CREATED),
                response(2L, ticketId, 5L, NotificationType.TICKET_STATUS_CHANGED)));

        mockMvc.perform(get("/api/tickets/" + ticketId + "/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type").value("TICKET_CREATED"))
                .andExpect(jsonPath("$[1].status").value("SENT"));

        mockMvc.perform(get("/api/tickets/999999/notifications"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveEnviarNotificacaoManualComAtorDoCabecalho() throws Exception {
        long ticketId = createCustomerAndTicket();
        clearInvocations(gateway);
        when(gateway.send(any())).thenReturn(Optional.of(response(7L, ticketId, 5L, NotificationType.MANUAL)));

        mockMvc.perform(post("/api/tickets/" + ticketId + "/notifications").contentType(JSON)
                        .header("X-Actor", "maria")
                        .content("{\"channel\":\"SMS\",\"subject\":\"Aviso\",\"message\":\"Mensagem manual\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.type").value("MANUAL"));

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(gateway).send(captor.capture());
        NotificationRequest sent = captor.getValue();
        assertThat(sent.ticketId()).isEqualTo(ticketId);
        assertThat(sent.channel()).isEqualTo(NotificationChannel.SMS);
        assertThat(sent.type()).isEqualTo(NotificationType.MANUAL);
        assertThat(sent.subject()).isEqualTo("Aviso");
        assertThat(sent.requestedBy()).isEqualTo("maria");
        assertThat(sent.recipientName()).startsWith("Cliente ");
    }

    @Test
    void deveResponder503QuandoMicrosservicoIndisponivelE400QuandoCorpoInvalido() throws Exception {
        long ticketId = createCustomerAndTicket();
        when(gateway.send(any())).thenReturn(Optional.empty());
        when(gateway.findByTicket(ticketId)).thenThrow(new NotificationServiceUnavailableException(new RuntimeException("down")));

        mockMvc.perform(post("/api/tickets/" + ticketId + "/notifications").contentType(JSON)
                        .content("{\"channel\":\"EMAIL\",\"subject\":\"Aviso\",\"message\":\"Mensagem\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Service Unavailable"));

        mockMvc.perform(get("/api/tickets/" + ticketId + "/notifications"))
                .andExpect(status().isServiceUnavailable());

        mockMvc.perform(post("/api/tickets/" + ticketId + "/notifications").contentType(JSON)
                        .content("{\"subject\":\"\",\"message\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void deveConsultarEAtualizarPreferenciasDoCliente() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String customerResponse = mockMvc.perform(post("/api/customers").contentType(JSON)
                        .content("{\"name\":\"Pref %s\",\"email\":\"pref.%s@example.com\"}".formatted(suffix, suffix)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long customerId = extractId(customerResponse);

        when(gateway.findPreferences(customerId))
                .thenReturn(new NotificationPreferenceResponse(customerId, true, true, true, false, null));
        when(gateway.updatePreferences(eq(customerId), any()))
                .thenReturn(new NotificationPreferenceResponse(customerId, true, false, true, true, Instant.now()));

        mockMvc.perform(get("/api/customers/" + customerId + "/notification-preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.persisted").value(false));

        mockMvc.perform(put("/api/customers/" + customerId + "/notification-preferences").contentType(JSON)
                        .content("{\"emailEnabled\":true,\"smsEnabled\":false,\"inAppEnabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.smsEnabled").value(false))
                .andExpect(jsonPath("$.persisted").value(true));

        mockMvc.perform(get("/api/customers/999999/notification-preferences"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/customers/" + customerId + "/notification-preferences").contentType(JSON)
                        .content("{\"emailEnabled\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveExporStatusDaIntegracaoComOMicrosservico() throws Exception {
        when(gateway.status()).thenReturn(new NotificationServiceStatus(true, true, "UP", "CLOSED", List.of("http://localhost:8081")));

        mockMvc.perform(get("/api/notifications/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.health").value("UP"))
                .andExpect(jsonPath("$.circuitState").value("CLOSED"))
                .andExpect(jsonPath("$.instances[0]").value("http://localhost:8081"));
    }

    private static long extractId(String json) {
        String marker = "\"id\":";
        int start = json.indexOf(marker) + marker.length();
        int end = json.indexOf(",", start);
        return Long.parseLong(json.substring(start, end).trim());
    }
}
