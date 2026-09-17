package com.pb.notification.notification;

import com.pb.notification.common.BusinessRuleException;
import com.pb.notification.common.ResourceNotFoundException;
import com.pb.notification.notification.dto.NotificationRequest;
import com.pb.notification.notification.dto.NotificationResponse;
import com.pb.notification.notification.dto.NotificationStatsResponse;
import com.pb.notification.preference.dto.NotificationPreferenceRequest;
import com.pb.notification.preference.NotificationPreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTest {

    @Autowired
    private NotificationService service;

    @Autowired
    private NotificationPreferenceService preferenceService;

    @Autowired
    private NotificationRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    private static NotificationRequest request(Long customerId, NotificationChannel channel, String email, String phone) {
        return new NotificationRequest(1L, customerId, "Ana Souza", email, phone, channel,
                NotificationType.TICKET_CREATED, "Ticket aberto", "Seu ticket foi aberto", "maria");
    }

    @Test
    void deveCriarNotificacaoPendenteEDespacharComSucesso() {
        NotificationResponse created = service.create(request(100L, NotificationChannel.EMAIL, "ana@example.com", null));

        assertThat(created.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(created.requestedBy()).isEqualTo("maria");

        NotificationResponse sent = service.dispatch(created.id());

        assertThat(sent.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(sent.attempts()).isEqualTo(1);
        assertThat(sent.sentAt()).isNotNull();
        assertThatThrownBy(() -> service.dispatch(created.id()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void deveIgnorarNotificacaoQuandoCanalDesabilitadoNasPreferencias() {
        preferenceService.update(200L, new NotificationPreferenceRequest(true, false, true));

        NotificationResponse skipped = service.create(request(200L, NotificationChannel.SMS, null, "11999990000"));

        assertThat(skipped.status()).isEqualTo(NotificationStatus.SKIPPED);
        assertThat(skipped.failureReason()).contains("SMS");
        assertThatThrownBy(() -> service.dispatch(skipped.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ignorada");
    }

    @Test
    void deveMarcarComoFalhaQuandoDestinatarioNaoTemContatoERespeitarLimite() {
        NotificationResponse created = service.create(request(300L, NotificationChannel.EMAIL, null, null));

        NotificationResponse first = service.dispatch(created.id());
        assertThat(first.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(first.attempts()).isEqualTo(1);
        assertThat(first.failureReason()).contains("sem contato");

        service.dispatch(created.id());
        service.dispatch(created.id());

        assertThatThrownBy(() -> service.dispatch(created.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("limite de 3 tentativas");
    }

    @Test
    void dispatchPendingDeveProcessarPendentesEFalhasRetentaveis() {
        service.create(request(400L, NotificationChannel.EMAIL, "a@example.com", null));
        service.create(request(400L, NotificationChannel.IN_APP, null, null));
        NotificationResponse failing = service.create(request(400L, NotificationChannel.SMS, null, null));

        int processed = service.dispatchPending();

        assertThat(processed).isEqualTo(3);
        assertThat(service.findById(failing.id()).status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(repository.countByStatus(NotificationStatus.SENT)).isEqualTo(2);

        assertThat(service.dispatchPending()).isEqualTo(1);
        assertThat(service.dispatchPending()).isEqualTo(1);
        assertThat(service.dispatchPending()).isZero();
        assertThat(service.findById(failing.id()).attempts()).isEqualTo(3);
    }

    @Test
    void deveMarcarComoLidaECalcularEstatisticas() {
        NotificationResponse inApp = service.create(request(500L, NotificationChannel.IN_APP, null, null));
        service.create(request(500L, NotificationChannel.EMAIL, "b@example.com", null));
        service.dispatch(inApp.id());

        NotificationStatsResponse before = service.stats();
        assertThat(before.total()).isEqualTo(2);
        assertThat(before.unread()).isEqualTo(1);
        assertThat(before.byStatus().get(NotificationStatus.SENT)).isEqualTo(1);
        assertThat(before.byStatus().get(NotificationStatus.PENDING)).isEqualTo(1);
        assertThat(before.byChannel().get(NotificationChannel.IN_APP)).isEqualTo(1);

        NotificationResponse read = service.markRead(inApp.id());
        assertThat(read.readAt()).isNotNull();
        assertThat(service.stats().unread()).isZero();
    }

    @Test
    void deveExcluirELancarNotFoundParaIdInexistente() {
        NotificationResponse created = service.create(request(600L, NotificationChannel.EMAIL, "c@example.com", null));

        service.delete(created.id());

        assertThatThrownBy(() -> service.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.dispatch(999999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveBuscarPorTicketEFiltros() {
        service.create(request(700L, NotificationChannel.EMAIL, "d@example.com", null));
        service.create(new NotificationRequest(2L, 700L, "Ana", "d@example.com", null,
                NotificationChannel.EMAIL, NotificationType.MANUAL, "Manual", "msg", null));

        assertThat(service.findByTicket(1L)).hasSize(1);
        assertThat(service.search(new NotificationFilter(null, 700L, null, null, null))).hasSize(2);
        assertThat(service.search(new NotificationFilter(null, null, null, null, NotificationType.MANUAL)))
                .singleElement()
                .satisfies(n -> assertThat(n.requestedBy()).isEqualTo("system"));
    }
}
