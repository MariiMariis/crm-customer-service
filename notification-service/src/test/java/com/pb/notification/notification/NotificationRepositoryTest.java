package com.pb.notification.notification;

import com.pb.notification.config.PersistenceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PersistenceConfig.class)
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository repository;

    private Notification pendingEmail;
    private Notification sentInApp;
    private Notification failedSms;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        pendingEmail = repository.save(new Notification(1L, 10L, "Ana", "ana@example.com", null,
                NotificationChannel.EMAIL, NotificationType.TICKET_CREATED, "Aberto", "msg", "system"));
        sentInApp = new Notification(1L, 10L, "Ana", null, null,
                NotificationChannel.IN_APP, NotificationType.TICKET_STATUS_CHANGED, "Status", "msg", "maria");
        sentInApp.markSent();
        sentInApp = repository.save(sentInApp);
        failedSms = new Notification(2L, 20L, "Bruno", null, null,
                NotificationChannel.SMS, NotificationType.MANUAL, "Manual", "msg", "maria");
        failedSms.markFailed("sem telefone");
        failedSms = repository.save(failedSms);
        repository.flush();
    }

    @Test
    void devePreencherCreatedAtAutomaticamente() {
        assertThat(pendingEmail.getCreatedAt()).isNotNull();
        assertThat(pendingEmail.getId()).isNotNull();
    }

    @Test
    void deveListarPorTicketOrdenadoPorDataDesc() {
        List<Notification> byTicket = repository.findByTicketIdOrderByCreatedAtDesc(1L);

        assertThat(byTicket).hasSize(2);
        assertThat(byTicket).extracting(Notification::getTicketId).containsOnly(1L);
        assertThat(repository.countByTicketId(2L)).isEqualTo(1);
    }

    @Test
    void deveEncontrarPendentesEFalhasAbaixoDoLimiteParaDespacho() {
        List<Notification> dispatchable = repository.findDispatchable(3, PageRequest.of(0, 10));

        assertThat(dispatchable).extracting(Notification::getId)
                .containsExactly(pendingEmail.getId(), failedSms.getId());

        List<Notification> withoutRetries = repository.findDispatchable(1, PageRequest.of(0, 10));
        assertThat(withoutRetries).extracting(Notification::getId).containsExactly(pendingEmail.getId());
    }

    @Test
    void deveAgruparContagensPorStatusECanal() {
        assertThat(repository.countGroupedByStatus())
                .extracting(NotificationStatusCount::getStatus, NotificationStatusCount::getTotal)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(NotificationStatus.PENDING, 1L),
                        org.assertj.core.groups.Tuple.tuple(NotificationStatus.SENT, 1L),
                        org.assertj.core.groups.Tuple.tuple(NotificationStatus.FAILED, 1L));

        assertThat(repository.countGroupedByChannel())
                .extracting(NotificationChannelCount::getChannel, NotificationChannelCount::getTotal)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(NotificationChannel.EMAIL, 1L),
                        org.assertj.core.groups.Tuple.tuple(NotificationChannel.IN_APP, 1L),
                        org.assertj.core.groups.Tuple.tuple(NotificationChannel.SMS, 1L));

        assertThat(repository.countByStatus(NotificationStatus.SENT)).isEqualTo(1);
        assertThat(repository.countByStatusAndReadAtIsNull(NotificationStatus.SENT)).isEqualTo(1);
    }

    @Test
    void deveFiltrarComSpecifications() {
        NotificationFilter byCustomerAndStatus = new NotificationFilter(null, 10L, NotificationStatus.SENT, null, null);
        assertThat(repository.findAll(NotificationSpecifications.withFilter(byCustomerAndStatus)))
                .extracting(Notification::getId).containsExactly(sentInApp.getId());

        NotificationFilter byChannel = new NotificationFilter(null, null, null, NotificationChannel.SMS, null);
        assertThat(repository.findAll(NotificationSpecifications.withFilter(byChannel)))
                .extracting(Notification::getId).containsExactly(failedSms.getId());

        NotificationFilter byType = new NotificationFilter(1L, null, null, null, NotificationType.TICKET_CREATED);
        assertThat(repository.findAll(NotificationSpecifications.withFilter(byType)))
                .extracting(Notification::getId).containsExactly(pendingEmail.getId());

        NotificationFilter empty = new NotificationFilter(null, null, null, null, null);
        assertThat(repository.findAll(NotificationSpecifications.withFilter(empty))).hasSize(3);
    }
}
