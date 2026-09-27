package com.pb.crm.notification.infrastructure.sender;

import com.pb.crm.notification.application.dispatch.EmailSender;
import com.pb.crm.notification.domain.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SimulatedEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SimulatedEmailSender.class);
    private static final String UNREACHABLE_DOMAIN = ".invalid";

    private final String senderName;
    private final String senderEmail;
    private final String frontendUrl;

    public SimulatedEmailSender(@Value("${crm.notification.sender-name:PB CRM}") String senderName,
                                @Value("${crm.notification.sender-email:no-reply@pbtech.com.br}") String senderEmail,
                                @Value("${crm.notification.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void send(Notification notification) {
        String address = notification.getRecipientEmail();
        if (address.toLowerCase().endsWith(UNREACHABLE_DOMAIN)) {
            throw new IllegalStateException("servidor SMTP recusou o destinatario " + address);
        }
        log.info("[EMAIL] de {} <{}> para {} <{}> | {} | {} | {}{}",
                senderName, senderEmail, notification.getRecipientName(), address,
                notification.getTitle(), notification.getMessage(), frontendUrl,
                notification.getLink() == null ? "" : notification.getLink());
    }
}
