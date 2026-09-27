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
    public static final String COMPANY_REPLICA_QUEUE = "sales.replica.company";
    public static final String CONTACT_REPLICA_QUEUE = "sales.replica.contact";
    public static final String LEAD_CONVERSION_REPLY_QUEUE = "sales.saga.lead-conversion";

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

    @Bean
    public Declarables companyReplicaQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(COMPANY_REPLICA_QUEUE,
                ConsumerQueue.on("accounts.events", "accounts.company.#")));
    }

    @Bean
    public Declarables contactReplicaQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(CONTACT_REPLICA_QUEUE,
                ConsumerQueue.on("accounts.events", "accounts.contact.#")));
    }

    @Bean
    public Declarables leadConversionReplyQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(LEAD_CONVERSION_REPLY_QUEUE,
                ConsumerQueue.on("accounts.events", "accounts.lead-account.*")));
    }
}
