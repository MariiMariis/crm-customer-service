package com.pb.crm.notification.domain;

import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.notification.domain.alert.Alert;
import com.pb.crm.notification.domain.alert.AlertRules;
import com.pb.crm.notification.domain.alert.RecipientRole;
import com.pb.crm.notification.domain.notification.Notification;
import com.pb.crm.notification.domain.notification.NotificationChannel;
import com.pb.crm.notification.domain.notification.NotificationStatus;
import com.pb.crm.notification.domain.notification.NotificationType;
import com.pb.crm.notification.domain.preference.NotificationPreference;
import com.pb.crm.notification.domain.recipient.Recipient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationDomainTest {

    private static final Recipient ANA = new Recipient(1L, "Ana Ribeiro", "ana@pbtech.com.br", 9L, true, false);
    private static final AlertRules.OpportunityFacts DEAL = new AlertRules.OpportunityFacts(
            42L, "Renovacao de notebooks", "Lojas Alfa", "Ana Ribeiro", new BigDecimal("55860.00"), "preco");

    @Test
    void rulesDefineRecipientsTextAndDeepLink() {
        Alert won = AlertRules.opportunityWon(DEAL);
        assertThat(won.type()).isEqualTo(NotificationType.OPPORTUNITY_WON);
        assertThat(won.recipients()).containsExactly(RecipientRole.OWNER, RecipientRole.OWNER_MANAGER);
        assertThat(won.message()).contains("Lojas Alfa").contains("R$ 55.860,00");
        assertThat(won.link()).isEqualTo("/opportunities/42");

        Alert approval = AlertRules.discountApprovalRequested(DEAL);
        assertThat(approval.recipients()).containsExactly(RecipientRole.OWNER_MANAGER);

        Alert assigned = AlertRules.leadAssigned(new AlertRules.LeadFacts(7L, "Renata Lopes", "Lojas Alfa", 90, "Ana"));
        assertThat(assigned.recipients()).containsExactly(RecipientRole.OWNER);
        assertThat(assigned.title()).isEqualTo("Novo lead atribuído: Renata Lopes");
        assertThat(AlertRules.opportunityLost(DEAL).message()).contains("Motivo: preco");
    }

    @Test
    void inAppIsDeliveredImmediatelyAndEmailRequestsDispatch() {
        Alert alert = AlertRules.opportunityWon(DEAL);
        NotificationPreference defaults = NotificationPreference.defaults(1L);

        Notification inApp = Notification.compose(alert, ANA, NotificationChannel.IN_APP, defaults, UUID.randomUUID());
        assertThat(inApp.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(inApp.isUnread()).isTrue();
        assertThat(inApp.pullEvents()).isEmpty();

        Notification email = Notification.compose(alert, ANA, NotificationChannel.EMAIL, defaults, UUID.randomUUID());
        assertThat(email.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(email.pullEvents()).containsExactly(Notification.DISPATCH_REQUESTED);
        email.markSent();
        assertThat(email.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThatThrownBy(email::markSent).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(email::markRead).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void emailIsSkippedWhenDisabledOrRecipientHasNoAddress() {
        Alert alert = AlertRules.opportunityWon(DEAL);

        Notification disabled = Notification.compose(alert, ANA, NotificationChannel.EMAIL,
                NotificationPreference.defaults(1L).withEmail(false), UUID.randomUUID());
        assertThat(disabled.getStatus()).isEqualTo(NotificationStatus.SKIPPED);
        assertThat(disabled.getSkipReason()).contains("desligado");
        assertThat(disabled.pullEvents()).isEmpty();

        Recipient noEmail = new Recipient(2L, "Sem Email", null, null, true, false);
        Notification missing = Notification.compose(alert, noEmail, NotificationChannel.EMAIL,
                NotificationPreference.defaults(2L), UUID.randomUUID());
        assertThat(missing.getStatus()).isEqualTo(NotificationStatus.SKIPPED);
    }

    @Test
    void markReadIsIdempotentForInApp() {
        Notification inApp = Notification.compose(AlertRules.opportunityLost(DEAL), ANA, NotificationChannel.IN_APP,
                NotificationPreference.defaults(1L), UUID.randomUUID());
        inApp.markRead();
        var firstRead = inApp.getReadAt();
        inApp.markRead();

        assertThat(inApp.isUnread()).isFalse();
        assertThat(inApp.getReadAt()).isEqualTo(firstRead);
    }
}
