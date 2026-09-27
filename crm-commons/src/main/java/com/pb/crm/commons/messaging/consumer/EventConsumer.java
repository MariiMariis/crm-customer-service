package com.pb.crm.commons.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pb.crm.commons.actor.MessageContext;
import com.pb.crm.commons.messaging.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;

public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    @FunctionalInterface
    public interface Handler {
        void handle(EventEnvelope envelope) throws Exception;
    }

    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;
    private final ProcessedEventStore processedEventStore;
    private final RetryRouter retryRouter;

    public EventConsumer(ObjectMapper objectMapper,
                         TransactionTemplate transactionTemplate,
                         ProcessedEventStore processedEventStore,
                         RetryRouter retryRouter) {
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
        this.processedEventStore = processedEventStore;
        this.retryRouter = retryRouter;
    }

    public void consume(Message message, String consumerName, Handler handler) {
        String queue = message.getMessageProperties().getConsumerQueue();
        EventEnvelope envelope;
        try {
            envelope = objectMapper.readValue(message.getBody(), EventEnvelope.class);
        } catch (IOException ex) {
            retryRouter.reroute(message, queue, ex, false);
            return;
        }
        MessageContext.Current context = new MessageContext.Current(
                envelope.actor(), envelope.correlationId(), envelope.eventId().toString());
        try {
            MessageContext.runWith(context, () -> transactionTemplate.executeWithoutResult(status -> {
                if (!processedEventStore.markProcessed(consumerName, envelope.eventId(), envelope.eventType())) {
                    log.info("Evento {} ({}) ja processado por {}; mensagem duplicada descartada",
                            envelope.eventId(), envelope.eventType(), consumerName);
                    return;
                }
                try {
                    handler.handle(envelope);
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }));
        } catch (NonRetryableEventException ex) {
            retryRouter.reroute(message, queue, ex, false);
        } catch (RuntimeException ex) {
            retryRouter.reroute(message, queue, ex, true);
        }
    }

    public <T> T payload(EventEnvelope envelope, Class<T> type) {
        try {
            return objectMapper.treeToValue(envelope.payload(), type);
        } catch (IOException ex) {
            throw new NonRetryableEventException("payload invalido para " + type.getSimpleName() + ": " + ex.getMessage(), ex);
        }
    }
}
