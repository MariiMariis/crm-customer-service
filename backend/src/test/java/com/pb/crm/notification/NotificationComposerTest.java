package com.pb.crm.notification;

import com.pb.crm.notification.dto.ManualNotificationRequest;
import com.pb.crm.notification.dto.NotificationRequest;
import com.pb.crm.ticket.TicketPriority;
import com.pb.crm.ticket.TicketStatus;
import com.pb.crm.ticket.event.TicketCreatedEvent;
import com.pb.crm.ticket.event.TicketInteractionAddedEvent;
import com.pb.crm.ticket.event.TicketSnapshot;
import com.pb.crm.ticket.event.TicketStatusChangedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationComposerTest {

    private final NotificationComposer composer = new NotificationComposer();

    private static TicketSnapshot snapshot() {
        return new TicketSnapshot(42L, "Erro ao acessar o sistema", TicketStatus.OPEN, TicketPriority.HIGH,
                7L, "Ana Souza", "ana@example.com", "11988887777", "Diego");
    }

    @Test
    void deveComporNotificacaoDeTicketCriadoPorEmail() {
        NotificationRequest request = composer.ticketCreated(new TicketCreatedEvent(snapshot(), "maria"));

        assertThat(request.ticketId()).isEqualTo(42L);
        assertThat(request.customerId()).isEqualTo(7L);
        assertThat(request.channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(request.type()).isEqualTo(NotificationType.TICKET_CREATED);
        assertThat(request.recipientEmail()).isEqualTo("ana@example.com");
        assertThat(request.recipientPhone()).isEqualTo("11988887777");
        assertThat(request.subject()).isEqualTo("Ticket #42 aberto: Erro ao acessar o sistema");
        assertThat(request.message()).contains("Ana Souza").contains("HIGH").contains("#42");
        assertThat(request.requestedBy()).isEqualTo("maria");
    }

    @Test
    void deveComporNotificacaoDeMudancaDeStatusComMotivo() {
        NotificationRequest request = composer.statusChanged(new TicketStatusChangedEvent(
                snapshot(), TicketStatus.OPEN, TicketStatus.RESOLVED, "cliente confirmou", "maria"));

        assertThat(request.type()).isEqualTo(NotificationType.TICKET_STATUS_CHANGED);
        assertThat(request.subject()).isEqualTo("Ticket #42 atualizado para RESOLVED");
        assertThat(request.message()).contains("de OPEN para RESOLVED").contains("Motivo: cliente confirmou.");
    }

    @Test
    void deveOmitirMotivoQuandoAusente() {
        NotificationRequest request = composer.statusChanged(new TicketStatusChangedEvent(
                snapshot(), TicketStatus.OPEN, TicketStatus.IN_PROGRESS, null, "system"));

        assertThat(request.message()).doesNotContain("Motivo");
    }

    @Test
    void deveComporInteracaoComoNotificacaoInApp() {
        NotificationRequest request = composer.interactionAdded(new TicketInteractionAddedEvent(
                snapshot(), "Diego", "Estamos analisando.", "diego"));

        assertThat(request.channel()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(request.type()).isEqualTo(NotificationType.TICKET_INTERACTION_ADDED);
        assertThat(request.subject()).isEqualTo("Nova interacao no ticket #42");
        assertThat(request.message()).isEqualTo("Diego escreveu no chamado \"Erro ao acessar o sistema\": Estamos analisando.");
    }

    @Test
    void deveComporNotificacaoManualETruncarTextosLongos() {
        String longSubject = "x".repeat(200);
        NotificationRequest request = composer.manual(snapshot(),
                new ManualNotificationRequest(NotificationChannel.SMS, longSubject, "Mensagem manual"), "maria");

        assertThat(request.channel()).isEqualTo(NotificationChannel.SMS);
        assertThat(request.type()).isEqualTo(NotificationType.MANUAL);
        assertThat(request.subject()).hasSize(160).endsWith("...");
        assertThat(request.message()).isEqualTo("Mensagem manual");
    }
}
