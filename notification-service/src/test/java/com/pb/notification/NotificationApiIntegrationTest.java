package com.pb.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationApiIntegrationTest {

    private static final String JSON = "application/json";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveExecutarFluxoCompletoDeNotificacoesEPreferencias() throws Exception {
        long customerId = 9001L;
        long ticketId = 7001L;

        mockMvc.perform(get("/api/notification-preferences/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.persisted").value(false))
                .andExpect(jsonPath("$.smsEnabled").value(true));

        mockMvc.perform(put("/api/notification-preferences/" + customerId)
                        .contentType(JSON)
                        .content("{\"emailEnabled\":true,\"smsEnabled\":false,\"inAppEnabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persisted").value(true))
                .andExpect(jsonPath("$.smsEnabled").value(false));

        String emailJson = """
                {"ticketId":%d,"customerId":%d,"recipientName":"Ana Souza","recipientEmail":"ana@example.com",
                 "channel":"EMAIL","type":"TICKET_CREATED","subject":"Ticket aberto","message":"Seu ticket foi aberto","requestedBy":"maria"}
                """.formatted(ticketId, customerId);
        String emailResponse = mockMvc.perform(post("/api/notifications").contentType(JSON).content(emailJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.requestedBy").value("maria"))
                .andExpect(jsonPath("$.attempts").value(0))
                .andReturn().getResponse().getContentAsString();
        long emailId = extractId(emailResponse);

        String smsJson = """
                {"ticketId":%d,"customerId":%d,"recipientName":"Ana Souza","recipientPhone":"11999990000",
                 "channel":"SMS","type":"TICKET_STATUS_CHANGED","subject":"Status alterado","message":"Seu ticket mudou de status"}
                """.formatted(ticketId, customerId);
        mockMvc.perform(post("/api/notifications").contentType(JSON).content(smsJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SKIPPED"))
                .andExpect(jsonPath("$.failureReason").value("canal SMS desabilitado nas preferencias do cliente"));

        mockMvc.perform(get("/api/notifications").param("ticketId", String.valueOf(ticketId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/notifications").param("ticketId", String.valueOf(ticketId)).param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(emailId));

        mockMvc.perform(post("/api/notifications/" + emailId + "/dispatch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.attempts").value(1))
                .andExpect(jsonPath("$.sentAt").exists());

        mockMvc.perform(post("/api/notifications/" + emailId + "/dispatch"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Business Rule Violation"));

        mockMvc.perform(patch("/api/notifications/" + emailId + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readAt").exists());

        mockMvc.perform(patch("/api/notifications/" + emailId + "/read"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/notifications/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.byStatus.SENT").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.byStatus.SKIPPED").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.byChannel.EMAIL").value(greaterThanOrEqualTo(1)));

        mockMvc.perform(delete("/api/notifications/" + emailId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications/" + emailId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveDespacharPendentesEmLoteViaApi() throws Exception {
        String json = """
                {"ticketId":7002,"customerId":9002,"recipientName":"Bruno","channel":"IN_APP","type":"MANUAL",
                 "subject":"Aviso","message":"Mensagem manual"}
                """;
        mockMvc.perform(post("/api/notifications").contentType(JSON).content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/notifications/dispatch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/notifications").param("ticketId", "7002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SENT"));
    }

    @Test
    void deveValidarCamposObrigatoriosERejeitarParametrosInvalidos() throws Exception {
        mockMvc.perform(post("/api/notifications").contentType(JSON)
                        .content("{\"ticketId\":1,\"channel\":\"EMAIL\",\"recipientEmail\":\"invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.details").isArray());

        mockMvc.perform(get("/api/notifications").param("status", "NAO_EXISTE"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/notification-preferences/1").contentType(JSON).content("{\"emailEnabled\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveExporHealthEInfoDoActuator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("notification-service"));
    }

    private static long extractId(String json) {
        String marker = "\"id\":";
        int start = json.indexOf(marker) + marker.length();
        int end = json.indexOf(",", start);
        return Long.parseLong(json.substring(start, end).trim());
    }
}
