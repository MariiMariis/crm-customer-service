package com.pb.crm.messagingtest;

import com.pb.crm.commons.actor.MessageContext;
import com.pb.crm.commons.messaging.DomainEventPublisher;
import com.pb.crm.commons.messaging.EventEnvelope;
import com.pb.crm.commons.messaging.consumer.ProcessedEventStore;
import com.pb.crm.commons.messaging.outbox.OutboxEvent;
import com.pb.crm.commons.messaging.outbox.OutboxEventRepository;
import com.pb.crm.commons.messaging.web.MessagingOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = MessagingTestApplication.class)
class MessagingIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @ServiceConnection
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.13-management");

    static {
        POSTGRES.start();
        RABBIT.start();
    }

    @Autowired
    private DomainEventPublisher publisher;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private ProcessedEventStore processedEventStore;

    @Autowired
    private MessagingTestApplication.RecordingListener listener;

    @Autowired
    private MessagingOperations operations;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void reset() {
        listener.reset();
        amqpAdmin.purgeQueue(MessagingTestApplication.QUEUE + ".dlq", false);
    }

    private void publishInTransaction(String eventType, Object payload, String actor) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                MessageContext.runWith(new MessageContext.Current(actor, null, null),
                        () -> publisher.publish(eventType, "Sample", 7L, payload)));
    }

    @Test
    void outboxEventIsRelayedWithConfirmsAndConsumedWithActorPropagation() {
        publishInTransaction("test.sample.created", Map.of("name", "Primeiro"), "vendedora.ana");

        await().atMost(Duration.ofSeconds(15)).until(() -> listener.received().size() == 1);

        EventEnvelope envelope = listener.received().get(0);
        assertThat(envelope.eventType()).isEqualTo("test.sample.created");
        assertThat(envelope.source()).isEqualTo("commons-test");
        assertThat(envelope.aggregateId()).isEqualTo("7");
        assertThat(envelope.payload().get("name").asText()).isEqualTo("Primeiro");
        assertThat(listener.actors()).containsExactly("vendedora.ana");

        OutboxEvent stored = outboxRepository.findById(envelope.eventId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(OutboxEvent.Status.PUBLISHED);
        assertThat(stored.getPublishedAt()).isNotNull();
    }

    @Test
    void publishingOutsideTransactionIsRejected() {
        assertThatThrownBy(() -> publisher.publish("test.sample.created", "Sample", 1L, Map.of()))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    @Test
    void duplicatedDeliveryIsProcessedOnlyOnce() {
        publishInTransaction("test.sample.updated", Map.of("name", "Duplicado"), "system");
        await().atMost(Duration.ofSeconds(15)).until(() -> listener.received().size() == 1);
        UUID eventId = listener.received().get(0).eventId();
        OutboxEvent stored = outboxRepository.findById(eventId).orElseThrow();

        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setMessageId(eventId.toString());
        Message duplicate = MessageBuilder.withBody(stored.getEnvelope().getBytes()).andProperties(properties).build();
        rabbitTemplate.send("", MessagingTestApplication.QUEUE, duplicate);
        rabbitTemplate.send("", MessagingTestApplication.QUEUE, duplicate);

        await().pollDelay(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(5)).until(() -> true);
        assertThat(listener.received()).hasSize(1);
        assertThat(processedEventStore.countProcessed(MessagingTestApplication.CONSUMER)).isGreaterThanOrEqualTo(1);
    }

    @Test
    void failingConsumerRetriesThroughDelayQueuesThenParksInDlqAndCanBeReplayed() {
        listener.failing(true);
        publishInTransaction("test.sample.failed", Map.of("name", "Quebrado"), "system");

        await().atMost(Duration.ofSeconds(20)).until(() -> dlqCount() == 1);
        assertThat(listener.attempts()).isEqualTo(3);

        List<MessagingOperations.QueueStatus> queues = operations.status().queues();
        assertThat(queues).anySatisfy(queue -> {
            assertThat(queue.name()).isEqualTo(MessagingTestApplication.QUEUE);
            assertThat(queue.deadLetters()).isEqualTo(1);
        });

        listener.failing(false);
        MessagingOperations.ReplayResult result = operations.replayDeadLetters(MessagingTestApplication.QUEUE, 10);
        assertThat(result.replayed()).isEqualTo(1);
        await().atMost(Duration.ofSeconds(15)).until(() -> listener.received().size() == 1);
        assertThat(dlqCount()).isZero();
    }

    @Test
    void invalidPayloadGoesStraightToDlqWithoutRetries() {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        rabbitTemplate.send("", MessagingTestApplication.QUEUE,
                MessageBuilder.withBody("nao e json".getBytes()).andProperties(properties).build());

        await().atMost(Duration.ofSeconds(10)).until(() -> dlqCount() == 1);
        assertThat(listener.attempts()).isZero();
    }

    private long dlqCount() {
        var info = amqpAdmin.getQueueInfo(MessagingTestApplication.QUEUE + ".dlq");
        return info == null ? 0 : info.getMessageCount();
    }
}
