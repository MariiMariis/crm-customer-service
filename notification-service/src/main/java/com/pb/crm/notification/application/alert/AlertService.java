package com.pb.crm.notification.application.alert;

import com.pb.crm.commons.messaging.DomainEventPublisher;
import com.pb.crm.notification.application.dispatch.DispatchRequested;
import com.pb.crm.notification.domain.alert.Alert;
import com.pb.crm.notification.domain.alert.RecipientRole;
import com.pb.crm.notification.domain.notification.Notification;
import com.pb.crm.notification.domain.notification.NotificationChannel;
import com.pb.crm.notification.domain.notification.NotificationRepository;
import com.pb.crm.notification.domain.preference.NotificationPreference;
import com.pb.crm.notification.domain.preference.NotificationPreferenceRepository;
import com.pb.crm.notification.domain.recipient.Recipient;
import com.pb.crm.notification.domain.recipient.RecipientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final RecipientRepository recipientRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationRepository notificationRepository;
    private final DomainEventPublisher eventPublisher;

    public AlertService(RecipientRepository recipientRepository,
                        NotificationPreferenceRepository preferenceRepository,
                        NotificationRepository notificationRepository,
                        DomainEventPublisher eventPublisher) {
        this.recipientRepository = recipientRepository;
        this.preferenceRepository = preferenceRepository;
        this.notificationRepository = notificationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public List<Notification> raise(Alert alert, Long ownerId, UUID sourceEventId) {
        if (ownerId == null) {
            log.info("Alerta {} ignorado: evento {} sem vendedor responsavel", alert.type(), sourceEventId);
            return List.of();
        }
        Recipient owner = recipientRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalStateException(
                        "vendedor %d ainda nao sincronizado a partir do team-service; nova tentativa agendada".formatted(ownerId)));
        List<Notification> created = new ArrayList<>();
        for (Recipient recipient : resolve(alert.recipients(), owner).values()) {
            if (!recipient.canReceive()) {
                continue;
            }
            NotificationPreference preference = preferenceRepository.findBySalesRep(recipient.id());
            for (NotificationChannel channel : NotificationChannel.values()) {
                created.add(persist(Notification.compose(alert, recipient, channel, preference, sourceEventId)));
            }
        }
        log.info("Alerta {} gerou {} notificacao(oes) a partir do evento {}", alert.type(), created.size(), sourceEventId);
        return created;
    }

    private Map<Long, Recipient> resolve(List<RecipientRole> roles, Recipient owner) {
        Map<Long, Recipient> recipients = new LinkedHashMap<>();
        for (RecipientRole role : roles) {
            switch (role) {
                case OWNER -> recipients.putIfAbsent(owner.id(), owner);
                case OWNER_MANAGER -> {
                    if (owner.managerId() == null) {
                        log.info("Vendedor #{} nao possui gestor; alerta ao gestor ignorado", owner.id());
                    } else {
                        recipientRepository.findById(owner.managerId())
                                .ifPresentOrElse(manager -> recipients.putIfAbsent(manager.id(), manager),
                                        () -> log.warn("Gestor #{} do vendedor #{} ainda nao sincronizado", owner.managerId(), owner.id()));
                    }
                }
            }
        }
        return recipients;
    }

    private Notification persist(Notification notification) {
        List<String> events = notification.pullEvents();
        Notification saved = notificationRepository.save(notification);
        events.forEach(event -> eventPublisher.publish(event, "Notification", saved.getId(),
                new DispatchRequested(saved.getId(), saved.getRecipientId(), saved.getChannel().name())));
        return saved;
    }
}
