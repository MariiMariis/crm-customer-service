package com.pb.crm.commons.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pb.crm.commons.actor.MessageContext;
import com.pb.crm.commons.actor.RequestActor;
import com.pb.crm.commons.messaging.DomainEventPublisher;
import com.pb.crm.commons.messaging.EventEnvelope;
import com.pb.crm.commons.messaging.MessagingProperties;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    private final MessagingProperties properties;

    public OutboxDomainEventPublisher(OutboxEventRepository repository,
                                      ObjectMapper objectMapper,
                                      MessagingProperties properties) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String eventType, String aggregateType, Object aggregateId, Object payload) {
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now();
        String correlationId = MessageContext.current()
                .map(MessageContext.Current::correlationId)
                .orElse(eventId.toString());
        String causationId = MessageContext.current()
                .map(MessageContext.Current::causationId)
                .orElse(null);
        EventEnvelope envelope = new EventEnvelope(
                eventId,
                eventType,
                EventEnvelope.CURRENT_VERSION,
                now,
                properties.source(),
                aggregateType,
                String.valueOf(aggregateId),
                correlationId,
                causationId,
                RequestActor.current(),
                objectMapper.valueToTree(payload)
        );
        repository.save(new OutboxEvent(
                eventId,
                properties.exchange(),
                eventType,
                eventType,
                aggregateType,
                String.valueOf(aggregateId),
                serialize(envelope),
                now
        ));
    }

    private String serialize(EventEnvelope envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("nao foi possivel serializar o evento " + envelope.eventType(), ex);
        }
    }
}
