package com.pb.crm.commons.messaging.web;

import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.commons.messaging.MessageHeaders;
import com.pb.crm.commons.messaging.MessagingProperties;
import com.pb.crm.commons.messaging.outbox.OutboxEvent;
import com.pb.crm.commons.messaging.outbox.OutboxEventRepository;
import com.pb.crm.commons.messaging.topology.ConsumerQueue;
import com.pb.crm.commons.messaging.topology.MessagingTopology;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class MessagingOperations {

    private final MessagingProperties properties;
    private final MessagingTopology topology;
    private final OutboxEventRepository outboxRepository;
    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;

    public MessagingOperations(MessagingProperties properties,
                               MessagingTopology topology,
                               OutboxEventRepository outboxRepository,
                               AmqpAdmin amqpAdmin,
                               RabbitTemplate rabbitTemplate) {
        this.properties = properties;
        this.topology = topology;
        this.outboxRepository = outboxRepository;
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public record QueueStatus(String name, Long messages, Integer consumers, long retrying, long deadLetters, boolean reachable,
                              List<ConsumerQueue.Subscription> subscriptions) {
    }

    public record OutboxStatus(long pending, long published, Instant oldestPendingAt) {
    }

    public record Status(String source, String exchange, OutboxStatus outbox, List<QueueStatus> queues) {
    }

    public record OutboxEntry(UUID id, String eventType, String aggregateType, String aggregateId, OutboxEvent.Status status,
                              int attempts, String lastError, Instant createdAt, Instant publishedAt) {
    }

    public record ReplayResult(String queue, int replayed) {
    }

    @Transactional(readOnly = true)
    public Status status() {
        OutboxStatus outbox = new OutboxStatus(
                outboxRepository.countByStatus(OutboxEvent.Status.PENDING),
                outboxRepository.countByStatus(OutboxEvent.Status.PUBLISHED),
                outboxRepository.oldestPendingCreatedAt().orElse(null));
        List<QueueStatus> queues = new ArrayList<>();
        for (ConsumerQueue queue : topology.consumerQueues()) {
            queues.add(queueStatus(queue));
        }
        return new Status(properties.source(), properties.exchange(), outbox, queues);
    }

    @Transactional(readOnly = true)
    public List<OutboxEntry> recentOutbox() {
        return outboxRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(event -> new OutboxEntry(event.getId(), event.getEventType(), event.getAggregateType(),
                        event.getAggregateId(), event.getStatus(), event.getAttempts(), event.getLastError(),
                        event.getCreatedAt(), event.getPublishedAt()))
                .toList();
    }

    public ReplayResult replayDeadLetters(String queueName, int max) {
        ConsumerQueue queue = topology.findByName(queueName)
                .orElseThrow(() -> new ResourceNotFoundException("fila de consumo nao encontrada: " + queueName));
        QueueInformation deadLetters = amqpAdmin.getQueueInfo(queue.deadLetterQueue());
        int limit = deadLetters == null ? 0 : Math.min(max, deadLetters.getMessageCount());
        int replayed = 0;
        while (replayed < limit) {
            Message dead = rabbitTemplate.receive(queue.deadLetterQueue());
            if (dead == null) {
                break;
            }
            Message retried = MessageBuilder.fromMessage(dead)
                    .setHeader(MessageHeaders.RETRY_COUNT, 0)
                    .removeHeader(MessageHeaders.EXCEPTION_CLASS)
                    .removeHeader(MessageHeaders.EXCEPTION_MESSAGE)
                    .removeHeader(MessageHeaders.FAILED_AT)
                    .build();
            rabbitTemplate.send("", queue.name(), retried);
            replayed++;
        }
        return new ReplayResult(queue.name(), replayed);
    }

    private QueueStatus queueStatus(ConsumerQueue queue) {
        try {
            QueueInformation main = amqpAdmin.getQueueInfo(queue.name());
            long retrying = IntStream.rangeClosed(1, topology.maxAttempts())
                    .mapToObj(queue::retryQueue)
                    .map(amqpAdmin::getQueueInfo)
                    .mapToLong(info -> info == null ? 0 : info.getMessageCount())
                    .sum();
            QueueInformation dlq = amqpAdmin.getQueueInfo(queue.deadLetterQueue());
            return new QueueStatus(queue.name(),
                    main == null ? null : (long) main.getMessageCount(),
                    main == null ? null : main.getConsumerCount(),
                    retrying,
                    dlq == null ? 0 : dlq.getMessageCount(),
                    main != null,
                    queue.subscriptions());
        } catch (RuntimeException ex) {
            return new QueueStatus(queue.name(), null, null, 0, 0, false, queue.subscriptions());
        }
    }
}
