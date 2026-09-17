package com.pb.crm.notification;

import com.pb.crm.ticket.event.TicketCreatedEvent;
import com.pb.crm.ticket.event.TicketInteractionAddedEvent;
import com.pb.crm.ticket.event.TicketStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TicketNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(TicketNotificationListener.class);

    private final NotificationGateway gateway;
    private final NotificationComposer composer;

    public TicketNotificationListener(NotificationGateway gateway, NotificationComposer composer) {
        this.gateway = gateway;
        this.composer = composer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketCreated(TicketCreatedEvent event) {
        gateway.send(composer.ticketCreated(event))
                .ifPresent(n -> log.info("Notificacao #{} ({}) registrada para o ticket #{}", n.id(), n.type(), n.ticketId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(TicketStatusChangedEvent event) {
        gateway.send(composer.statusChanged(event))
                .ifPresent(n -> log.info("Notificacao #{} ({}) registrada para o ticket #{}", n.id(), n.type(), n.ticketId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInteractionAdded(TicketInteractionAddedEvent event) {
        gateway.send(composer.interactionAdded(event))
                .ifPresent(n -> log.info("Notificacao #{} ({}) registrada para o ticket #{}", n.id(), n.type(), n.ticketId()));
    }
}
