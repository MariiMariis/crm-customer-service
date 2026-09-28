package com.pb.crm.commons.messaging.outbox;

import com.pb.crm.commons.messaging.MessageHeaders;
import com.pb.crm.commons.messaging.MessagingProperties;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final TransactionTemplate transactionTemplate;
    private final MessagingProperties properties;

    public OutboxRelay(OutboxEventRepository repository,
                       RabbitTemplate rabbitTemplate,
                       TransactionTemplate transactionTemplate,
                       MessagingProperties properties) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.transactionTemplate = transactionTemplate;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${crm.messaging.outbox.poll-interval:500ms}")
    public void relayScheduled() {
        if (properties.outbox().relayEnabled()) {
            relayPending();
        }
    }

    public int relayPending() {
        try (Scope ignored = OutboxTracing.untracedPolling()) {
            return relayBatch();
        }
    }

    private int relayBatch() {
        Integer published = transactionTemplate.execute(status -> {
            List<OutboxEvent> batch = repository.lockPendingBatch(properties.outbox().batchSize());
            int count = 0;
            for (OutboxEvent event : batch) {
                Span span = OutboxTracing.startPublishSpan(event);
                try (Scope ignored = span.makeCurrent()) {
                    send(event);
                    event.markPublished();
                    count++;
                } catch (Exception ex) {
                    OutboxTracing.recordFailure(span, ex);
                    event.markFailedAttempt(rootMessage(ex));
                    log.warn("Falha ao publicar o evento {} ({}) na tentativa {}: {}",
                            event.getId(), event.getEventType(), event.getAttempts(), rootMessage(ex));
                    break;
                } finally {
                    span.end();
                }
            }
            return count;
        });
        if (published != null && published > 0) {
            log.debug("Relay da outbox publicou {} evento(s)", published);
        }
        return published == null ? 0 : published;
    }

    private void send(OutboxEvent event) throws Exception {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        messageProperties.setMessageId(event.getId().toString());
        messageProperties.setType(event.getEventType());
        messageProperties.setTimestamp(Date.from(event.getCreatedAt()));
        messageProperties.setAppId(properties.source());
        messageProperties.setHeader(MessageHeaders.EVENT_TYPE, event.getEventType());
        messageProperties.setHeader(MessageHeaders.SOURCE, properties.source());
        messageProperties.setHeader(MessageHeaders.AGGREGATE_TYPE, event.getAggregateType());
        messageProperties.setHeader(MessageHeaders.AGGREGATE_ID, event.getAggregateId());
        Message message = MessageBuilder.withBody(event.getEnvelope().getBytes(StandardCharsets.UTF_8))
                .andProperties(messageProperties)
                .build();
        CorrelationData correlation = new CorrelationData(event.getId().toString());
        rabbitTemplate.send(event.getExchange(), event.getRoutingKey(), message, correlation);
        CorrelationData.Confirm confirm = correlation.getFuture()
                .get(properties.outbox().confirmTimeout().toMillis(), TimeUnit.MILLISECONDS);
        if (!confirm.isAck()) {
            throw new IllegalStateException("broker recusou a mensagem: " + confirm.getReason());
        }
        if (correlation.getReturned() != null) {
            log.warn("Evento {} publicado em {} sem fila vinculada para a chave {}",
                    event.getEventType(), event.getExchange(), event.getRoutingKey());
        }
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getClass().getSimpleName() + ": " + current.getMessage();
    }
}
