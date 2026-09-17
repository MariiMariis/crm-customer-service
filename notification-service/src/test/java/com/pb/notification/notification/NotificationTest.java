package com.pb.notification.notification;

import com.pb.notification.common.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationTest {

    private static Notification email(String recipientEmail) {
        return new Notification(1L, 10L, "Ana Souza", recipientEmail, null,
                NotificationChannel.EMAIL, NotificationType.TICKET_CREATED,
                "Ticket aberto", "Seu ticket foi aberto", null);
    }

    @Test
    void deveNascerPendenteComAtorPadrao() {
        Notification notification = email("ana@example.com");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getAttempts()).isZero();
        assertThat(notification.getRequestedBy()).isEqualTo("system");
        assertThat(notification.hasRecipientContact()).isTrue();
        assertThat(notification.recipientAddress()).isEqualTo("ana@example.com");
    }

    @Test
    void deveMarcarComoEnviadaEIncrementarTentativas() {
        Notification notification = email("ana@example.com");

        notification.markSent();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getAttempts()).isEqualTo(1);
        assertThat(notification.getSentAt()).isNotNull();
        assertThat(notification.getFailureReason()).isNull();
    }

    @Test
    void deveRegistrarFalhaComMotivo() {
        Notification notification = email(null);

        assertThat(notification.hasRecipientContact()).isFalse();

        notification.markFailed("destinatario sem e-mail");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getAttempts()).isEqualTo(1);
        assertThat(notification.getFailureReason()).isEqualTo("destinatario sem e-mail");
    }

    @Test
    void naoDeveReenviarNotificacaoJaEnviada() {
        Notification notification = email("ana@example.com");
        notification.markSent();

        assertThatThrownBy(() -> notification.assertDispatchable(3))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ja foi enviada");
    }

    @Test
    void deveRespeitarLimiteDeTentativas() {
        Notification notification = email(null);
        notification.markFailed("falha 1");
        notification.markFailed("falha 2");

        assertThatThrownBy(() -> notification.assertDispatchable(2))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("limite de 2 tentativas");
    }

    @Test
    void deveIgnorarApenasQuandoPendente() {
        Notification notification = email("ana@example.com");
        notification.skip("canal EMAIL desabilitado");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SKIPPED);
        assertThat(notification.getFailureReason()).contains("desabilitado");
        assertThatThrownBy(() -> notification.assertDispatchable(3))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ignorada");
        assertThatThrownBy(() -> notification.skip("de novo"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void somenteNotificacaoEnviadaPodeSerLidaUmaUnicaVez() {
        Notification notification = email("ana@example.com");

        assertThatThrownBy(notification::markRead)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("apenas notificacoes enviadas");

        notification.markSent();
        notification.markRead();
        assertThat(notification.getReadAt()).isNotNull();

        assertThatThrownBy(notification::markRead)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ja foi marcada como lida");
    }

    @Test
    void canalSmsExigeTelefoneEInAppNaoExigeContato() {
        Notification sms = new Notification(1L, 10L, "Ana", null, "11999990000",
                NotificationChannel.SMS, NotificationType.MANUAL, "Assunto", "Mensagem", "maria");
        Notification inApp = new Notification(1L, 10L, "Ana", null, null,
                NotificationChannel.IN_APP, NotificationType.MANUAL, "Assunto", "Mensagem", "maria");

        assertThat(sms.hasRecipientContact()).isTrue();
        assertThat(sms.recipientAddress()).isEqualTo("11999990000");
        assertThat(sms.getRequestedBy()).isEqualTo("maria");
        assertThat(inApp.hasRecipientContact()).isTrue();
        assertThat(inApp.recipientAddress()).isEqualTo("cliente#10");
    }
}
