package com.pb.crm.commons.messaging.topology;

import com.pb.crm.commons.messaging.MessagingProperties;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.TopicExchange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MessagingTopology {

    private final MessagingProperties properties;
    private final List<ConsumerQueue> consumerQueues = new ArrayList<>();

    public MessagingTopology(MessagingProperties properties) {
        this.properties = properties;
    }

    public Declarables ownExchange() {
        List<Declarable> declarables = new ArrayList<>();
        if (properties.exchange() != null && !properties.exchange().isBlank()) {
            declarables.add(new TopicExchange(properties.exchange(), true, false));
        }
        return new Declarables(declarables);
    }

    public Declarables consumer(ConsumerQueue queue) {
        consumerQueues.add(queue);
        return queue.declarables(properties.retry().delays());
    }

    public List<ConsumerQueue> consumerQueues() {
        return Collections.unmodifiableList(consumerQueues);
    }

    public Optional<ConsumerQueue> findByName(String name) {
        return consumerQueues.stream().filter(queue -> queue.name().equals(name)).findFirst();
    }

    public int maxAttempts() {
        return properties.retry().delays().size();
    }
}
