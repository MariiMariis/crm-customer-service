package com.pb.crm.notification.infrastructure.messaging;

import com.pb.crm.commons.messaging.EventEnvelope;
import com.pb.crm.commons.messaging.consumer.EventConsumer;
import com.pb.crm.commons.messaging.consumer.NonRetryableEventException;
import com.pb.crm.notification.application.alert.AlertService;
import com.pb.crm.notification.application.dispatch.DispatchRequested;
import com.pb.crm.notification.application.dispatch.DispatchService;
import com.pb.crm.notification.domain.alert.Alert;
import com.pb.crm.notification.domain.alert.AlertRules;
import com.pb.crm.notification.domain.recipient.Recipient;
import com.pb.crm.notification.domain.recipient.RecipientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListeners {

    private static final Logger log = LoggerFactory.getLogger(NotificationListeners.class);

    private final EventConsumer eventConsumer;
    private final RecipientRepository recipientRepository;
    private final AlertService alertService;
    private final DispatchService dispatchService;

    public NotificationListeners(EventConsumer eventConsumer,
                                 RecipientRepository recipientRepository,
                                 AlertService alertService,
                                 DispatchService dispatchService) {
        this.eventConsumer = eventConsumer;
        this.recipientRepository = recipientRepository;
        this.alertService = alertService;
        this.dispatchService = dispatchService;
    }

    @RabbitListener(queues = NotificationMessagingConfig.RECIPIENT_REPLICA_QUEUE)
    public void onSalesRepEvent(Message message) {
        eventConsumer.consume(message, NotificationMessagingConfig.RECIPIENT_REPLICA_QUEUE, envelope -> {
            SalesPayloads.SalesRep payload = eventConsumer.payload(envelope, SalesPayloads.SalesRep.class);
            boolean applied = recipientRepository.upsert(new Recipient(payload.id(), payload.name(), payload.email(),
                    payload.managerId(), payload.active(), payload.archived()), envelope.occurredAt());
            log.info("Destinatario #{} {} a partir de {}", payload.id(),
                    applied ? "atualizado" : "mantido (evento mais antigo)", envelope.eventType());
        });
    }

    @RabbitListener(queues = NotificationMessagingConfig.ALERTS_QUEUE)
    public void onSalesEvent(Message message) {
        eventConsumer.consume(message, NotificationMessagingConfig.ALERTS_QUEUE, envelope -> {
            switch (envelope.eventType()) {
                case "sales.lead.assigned" -> {
                    SalesPayloads.Lead lead = eventConsumer.payload(envelope, SalesPayloads.Lead.class);
                    raise(AlertRules.leadAssigned(new AlertRules.LeadFacts(lead.id(), lead.fullName(),
                            lead.companyName(), lead.score(), lead.ownerName())), lead.ownerId(), envelope);
                }
                case "sales.opportunity.discount-approval-requested" ->
                        raise(AlertRules.discountApprovalRequested(opportunityFacts(envelope)), ownerOf(envelope), envelope);
                case "sales.opportunity.won" ->
                        raise(AlertRules.opportunityWon(opportunityFacts(envelope)), ownerOf(envelope), envelope);
                case "sales.opportunity.lost" ->
                        raise(AlertRules.opportunityLost(opportunityFacts(envelope)), ownerOf(envelope), envelope);
                default -> throw new NonRetryableEventException("evento sem regra de alerta: " + envelope.eventType());
            }
        });
    }

    @RabbitListener(queues = NotificationMessagingConfig.DISPATCH_QUEUE,
            concurrency = "${crm.notification.dispatch.concurrency:2}-${crm.notification.dispatch.max-concurrency:6}")
    public void onDispatchRequested(Message message) {
        eventConsumer.consume(message, NotificationMessagingConfig.DISPATCH_QUEUE, envelope -> {
            DispatchRequested command = eventConsumer.payload(envelope, DispatchRequested.class);
            dispatchService.dispatch(command.notificationId());
        });
    }

    private void raise(Alert alert, Long ownerId, EventEnvelope envelope) {
        alertService.raise(alert, ownerId, envelope.eventId());
    }

    private AlertRules.OpportunityFacts opportunityFacts(EventEnvelope envelope) {
        SalesPayloads.Opportunity opportunity = eventConsumer.payload(envelope, SalesPayloads.Opportunity.class);
        return new AlertRules.OpportunityFacts(opportunity.id(), opportunity.title(), opportunity.companyName(),
                opportunity.ownerName(), opportunity.amount(), opportunity.lossReason());
    }

    private Long ownerOf(EventEnvelope envelope) {
        return eventConsumer.payload(envelope, SalesPayloads.Opportunity.class).ownerId();
    }
}
