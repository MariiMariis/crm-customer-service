package com.pb.crm.commons.messaging.consumer;

import com.pb.crm.commons.messaging.MessageHeaders;
import com.pb.crm.commons.messaging.topology.MessagingTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;

public class RetryRouter {

    private static final Logger log = LoggerFactory.getLogger(RetryRouter.class);

    private final RabbitTemplate rabbitTemplate;
    private final MessagingTopology topology;

    public RetryRouter(RabbitTemplate rabbitTemplate, MessagingTopology topology) {
        this.rabbitTemplate = rabbitTemplate;
        this.topology = topology;
    }

    public void reroute(Message message, String queue, Exception failure, boolean retryable) {
        int attempt = retryCount(message) + 1;
        Throwable root = rootCause(failure);
        if (retryable && attempt <= topology.maxAttempts()) {
            String target = queue + ".retry." + attempt;
            Message copy = MessageBuilder.fromMessage(message)
                    .setHeader(MessageHeaders.RETRY_COUNT, attempt)
                    .setHeader(MessageHeaders.EXCEPTION_MESSAGE, truncate(root.getMessage()))
                    .build();
            rabbitTemplate.send("", target, copy);
            log.warn("Mensagem {} da fila {} falhou ({}); tentativa {} de {} agendada em {}",
                    message.getMessageProperties().getMessageId(), queue, root.getMessage(), attempt,
                    topology.maxAttempts(), target);
            return;
        }
        Message dead = MessageBuilder.fromMessage(message)
                .setHeader(MessageHeaders.RETRY_COUNT, attempt - 1)
                .setHeader(MessageHeaders.ORIGINAL_QUEUE, queue)
                .setHeader(MessageHeaders.EXCEPTION_CLASS, root.getClass().getName())
                .setHeader(MessageHeaders.EXCEPTION_MESSAGE, truncate(root.getMessage()))
                .setHeader(MessageHeaders.FAILED_AT, Instant.now().toString())
                .build();
        rabbitTemplate.send("", queue + ".dlq", dead);
        log.error("Mensagem {} da fila {} enviada para a DLQ apos {} tentativa(s): {}",
                message.getMessageProperties().getMessageId(), queue, attempt - 1, root.getMessage());
    }

    public static int retryCount(Message message) {
        Object value = message.getMessageProperties().getHeaders().get(MessageHeaders.RETRY_COUNT);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private static String truncate(String value) {
        if (value == null) {
            return "sem mensagem";
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }
}
