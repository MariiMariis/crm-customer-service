package com.pb.crm.commons.messaging.topology;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public record ConsumerQueue(String name, List<Subscription> subscriptions) {

    public record Subscription(String exchange, String routingKey) {
    }

    public static ConsumerQueue of(String name, Subscription... subscriptions) {
        return new ConsumerQueue(name, List.of(subscriptions));
    }

    public static Subscription on(String exchange, String routingKey) {
        return new Subscription(exchange, routingKey);
    }

    public String retryQueue(int attempt) {
        return name + ".retry." + attempt;
    }

    public String deadLetterQueue() {
        return name + ".dlq";
    }

    public Declarables declarables(List<Duration> retryDelays) {
        List<Declarable> declarables = new ArrayList<>();
        Queue main = QueueBuilder.durable(name).build();
        declarables.add(main);
        for (int attempt = 1; attempt <= retryDelays.size(); attempt++) {
            declarables.add(QueueBuilder.durable(retryQueue(attempt))
                    .ttl((int) retryDelays.get(attempt - 1).toMillis())
                    .deadLetterExchange("")
                    .deadLetterRoutingKey(name)
                    .build());
        }
        declarables.add(QueueBuilder.durable(deadLetterQueue()).build());
        for (Subscription subscription : subscriptions) {
            TopicExchange exchange = new TopicExchange(subscription.exchange(), true, false);
            declarables.add(exchange);
            Binding binding = BindingBuilder.bind(main).to(exchange).with(subscription.routingKey());
            declarables.add(binding);
        }
        return new Declarables(declarables);
    }
}
