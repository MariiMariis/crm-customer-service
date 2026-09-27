package com.pb.crm.sales.infrastructure.messaging;

import com.pb.crm.commons.messaging.topology.ConsumerQueue;
import com.pb.crm.commons.messaging.topology.MessagingTopology;
import org.springframework.amqp.core.Declarables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SalesMessagingConfig {

    public static final String SALES_REP_REPLICA_QUEUE = "sales.replica.salesrep";
    public static final String PRODUCT_REPLICA_QUEUE = "sales.replica.product";

    @Bean
    public Declarables salesRepReplicaQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(SALES_REP_REPLICA_QUEUE,
                ConsumerQueue.on("team.events", "team.salesrep.#")));
    }

    @Bean
    public Declarables productReplicaQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(PRODUCT_REPLICA_QUEUE,
                ConsumerQueue.on("catalog.events", "catalog.product.#")));
    }
}
