package com.pb.notification.notification;

import com.pb.notification.config.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationService notificationService;
    private final NotificationProperties properties;

    public NotificationDispatcher(NotificationService notificationService, NotificationProperties properties) {
        this.notificationService = notificationService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.notifications.dispatch.fixed-delay:5000}", initialDelayString = "${app.notifications.dispatch.fixed-delay:5000}")
    public void dispatchPendingNotifications() {
        if (!properties.dispatch().auto()) {
            return;
        }
        int processed = notificationService.dispatchPending();
        if (processed > 0) {
            log.info("Dispatcher processou {} notificacao(oes) pendente(s)", processed);
        }
    }
}
