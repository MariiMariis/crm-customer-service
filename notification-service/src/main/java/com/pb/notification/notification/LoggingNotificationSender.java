package com.pb.notification.notification;

import com.pb.notification.config.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    private final NotificationProperties properties;

    public LoggingNotificationSender(NotificationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void send(Notification notification) {
        if (!notification.hasRecipientContact()) {
            throw new IllegalStateException(
                    "destinatario sem contato para o canal " + notification.getChannel());
        }
        log.info("[{}] de {} <{}> para {} <{}> | ticket #{} | {} | {}",
                notification.getChannel(),
                properties.senderName(),
                properties.senderEmail(),
                notification.getRecipientName(),
                notification.recipientAddress(),
                notification.getTicketId(),
                notification.getSubject(),
                notification.getMessage());
    }
}
