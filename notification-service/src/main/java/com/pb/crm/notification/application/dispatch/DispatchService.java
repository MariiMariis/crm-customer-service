package com.pb.crm.notification.application.dispatch;

import com.pb.crm.commons.messaging.consumer.NonRetryableEventException;
import com.pb.crm.notification.domain.notification.Notification;
import com.pb.crm.notification.domain.notification.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    private final NotificationRepository notificationRepository;
    private final EmailSender emailSender;

    public DispatchService(NotificationRepository notificationRepository, EmailSender emailSender) {
        this.notificationRepository = notificationRepository;
        this.emailSender = emailSender;
    }

    @Transactional
    public boolean dispatch(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NonRetryableEventException("notificacao %d inexistente".formatted(notificationId)));
        if (!notification.awaitsDispatch()) {
            log.info("Notificacao #{} ja esta {}; despacho ignorado", notificationId, notification.getStatus());
            return false;
        }
        emailSender.send(notification);
        notification.markSent();
        notificationRepository.save(notification);
        return true;
    }
}
