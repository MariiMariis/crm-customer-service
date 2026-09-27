package com.pb.crm.accounts.infrastructure.messaging;

import com.pb.crm.commons.messaging.topology.ConsumerQueue;
import com.pb.crm.commons.messaging.topology.MessagingTopology;
import org.springframework.amqp.core.Declarables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountsMessagingConfig {

    public static final String SALES_REP_REPLICA_QUEUE = "accounts.replica.salesrep";

    @Bean
    public Declarables salesRepReplicaQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(SALES_REP_REPLICA_QUEUE,
                ConsumerQueue.on("team.events", "team.salesrep.#")));
    }
}
