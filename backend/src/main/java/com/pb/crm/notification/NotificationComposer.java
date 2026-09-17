package com.pb.crm.notification;

import com.pb.crm.notification.dto.ManualNotificationRequest;
import com.pb.crm.notification.dto.NotificationRequest;
import com.pb.crm.ticket.event.TicketCreatedEvent;
import com.pb.crm.ticket.event.TicketInteractionAddedEvent;
import com.pb.crm.ticket.event.TicketSnapshot;
import com.pb.crm.ticket.event.TicketStatusChangedEvent;
import org.springframework.stereotype.Component;

@Component
public class NotificationComposer {

    public NotificationRequest ticketCreated(TicketCreatedEvent event) {
        TicketSnapshot ticket = event.ticket();
        String subject = "Ticket #%d aberto: %s".formatted(ticket.id(), ticket.subject());
        String message = "Ola %s, recebemos o seu chamado \"%s\" com prioridade %s. Acompanhe o andamento pelo numero #%d."
                .formatted(ticket.customerName(), ticket.subject(), ticket.priority(), ticket.id());
        return build(ticket, NotificationChannel.EMAIL, NotificationType.TICKET_CREATED, subject, message, event.actor());
    }

    public NotificationRequest statusChanged(TicketStatusChangedEvent event) {
        TicketSnapshot ticket = event.ticket();
        String subject = "Ticket #%d atualizado para %s".formatted(ticket.id(), event.toStatus());
        String reason = event.reason() == null || event.reason().isBlank() ? "" : " Motivo: " + event.reason() + ".";
        String message = "Ola %s, o status do seu chamado \"%s\" mudou de %s para %s.%s"
                .formatted(ticket.customerName(), ticket.subject(), event.fromStatus(), event.toStatus(), reason);
        return build(ticket, NotificationChannel.EMAIL, NotificationType.TICKET_STATUS_CHANGED, subject, message, event.actor());
    }

    public NotificationRequest interactionAdded(TicketInteractionAddedEvent event) {
        TicketSnapshot ticket = event.ticket();
        String subject = "Nova interacao no ticket #%d".formatted(ticket.id());
        String message = "%s escreveu no chamado \"%s\": %s".formatted(event.author(), ticket.subject(), event.message());
        return build(ticket, NotificationChannel.IN_APP, NotificationType.TICKET_INTERACTION_ADDED, subject, message, event.actor());
    }

    public NotificationRequest manual(TicketSnapshot ticket, ManualNotificationRequest request, String actor) {
        return build(ticket, request.channel(), NotificationType.MANUAL, request.subject(), request.message(), actor);
    }

    private static NotificationRequest build(TicketSnapshot ticket,
                                             NotificationChannel channel,
                                             NotificationType type,
                                             String subject,
                                             String message,
                                             String actor) {
        return new NotificationRequest(
                ticket.id(),
                ticket.customerId(),
                ticket.customerName(),
                ticket.customerEmail(),
                ticket.customerPhone(),
                channel,
                type,
                truncate(subject, 160),
                truncate(message, 2000),
                actor
        );
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}
