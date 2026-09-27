package com.pb.crm.sales.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pb.crm.commons.messaging.EventEnvelope;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @ServiceConnection
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.13-management");

    static {
        POSTGRES.start();
        RABBIT.start();
    }

    @Autowired
    protected RabbitTemplate testRabbitTemplate;

    @Autowired
    protected AmqpAdmin testAmqpAdmin;

    @Autowired
    protected ObjectMapper testObjectMapper;

    protected EventEnvelope envelope(String eventType, String aggregateType, Object aggregateId, Object payload, Instant occurredAt) {
        UUID eventId = UUID.randomUUID();
        return new EventEnvelope(eventId, eventType, EventEnvelope.CURRENT_VERSION, occurredAt, "integration-test",
                aggregateType, String.valueOf(aggregateId), eventId.toString(), null, "integration-test",
                testObjectMapper.valueToTree(payload));
    }

    protected void publish(String exchange, EventEnvelope envelope) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setMessageId(envelope.eventId().toString());
            properties.setType(envelope.eventType());
            Message message = MessageBuilder.withBody(testObjectMapper.writeValueAsBytes(envelope))
                    .andProperties(properties)
                    .build();
            testRabbitTemplate.send(exchange, envelope.eventType(), message);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected String bindProbe(String exchange, String routingPattern) {
        Queue queue = new AnonymousQueue();
        TopicExchange topic = new TopicExchange(exchange, true, false);
        testAmqpAdmin.declareExchange(topic);
        testAmqpAdmin.declareQueue(queue);
        testAmqpAdmin.declareBinding(BindingBuilder.bind(queue).to(topic).with(routingPattern));
        return queue.getName();
    }

    protected Optional<EventEnvelope> receive(String queue) {
        Message message = testRabbitTemplate.receive(queue);
        if (message == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(testObjectMapper.readValue(message.getBody(), EventEnvelope.class));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
