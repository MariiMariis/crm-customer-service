package com.pb.crm.accounts.infrastructure.messaging;

import com.pb.crm.commons.messaging.topology.ConsumerQueue;
import com.pb.crm.commons.messaging.topology.MessagingTopology;
import org.springframework.amqp.core.Declarables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountsMessagingConfig {

    public static final String SALES_REP_REPLICA_QUEUE = "accounts.replica.salesrep";
    public static final String LEAD_CONVERSION_QUEUE = "accounts.saga.lead-conversion";

    @Bean
    public Declarables leadConversionQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(LEAD_CONVERSION_QUEUE,
                ConsumerQueue.on("sales.events", "sales.lead.conversion-requested")));
    }

    @Bean
    public Declarables salesRepReplicaQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(SALES_REP_REPLICA_QUEUE,
                ConsumerQueue.on("team.events", "team.salesrep.#")));
    }
}
