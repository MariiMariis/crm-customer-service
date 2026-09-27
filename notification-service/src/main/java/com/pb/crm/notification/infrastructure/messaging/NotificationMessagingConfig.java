package com.pb.crm.notification.infrastructure.messaging;

import com.pb.crm.commons.messaging.topology.ConsumerQueue;
import com.pb.crm.commons.messaging.topology.MessagingTopology;
import org.springframework.amqp.core.Declarables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationMessagingConfig {

    public static final String RECIPIENT_REPLICA_QUEUE = "notification.replica.salesrep";
    public static final String ALERTS_QUEUE = "notification.alerts";
    public static final String DISPATCH_QUEUE = "notification.dispatch";

    @Bean
    public Declarables recipientReplicaQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(RECIPIENT_REPLICA_QUEUE,
                ConsumerQueue.on("team.events", "team.salesrep.#")));
    }

    @Bean
    public Declarables alertsQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(ALERTS_QUEUE,
                ConsumerQueue.on("sales.events", "sales.lead.assigned"),
                ConsumerQueue.on("sales.events", "sales.opportunity.discount-approval-requested"),
                ConsumerQueue.on("sales.events", "sales.opportunity.won"),
                ConsumerQueue.on("sales.events", "sales.opportunity.lost")));
    }

    @Bean
    public Declarables dispatchQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(DISPATCH_QUEUE,
                ConsumerQueue.on("notification.events", "notification.dispatch-requested")));
    }
}
