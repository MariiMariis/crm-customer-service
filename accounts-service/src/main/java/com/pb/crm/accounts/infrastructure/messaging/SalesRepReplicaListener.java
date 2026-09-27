package com.pb.crm.accounts.infrastructure.messaging;

import com.pb.crm.accounts.domain.salesrep.SalesRepRef;
import com.pb.crm.accounts.domain.salesrep.SalesRepRefRepository;
import com.pb.crm.commons.messaging.consumer.EventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SalesRepReplicaListener {

    private static final Logger log = LoggerFactory.getLogger(SalesRepReplicaListener.class);

    private final EventConsumer eventConsumer;
    private final SalesRepRefRepository repository;

    public SalesRepReplicaListener(EventConsumer eventConsumer, SalesRepRefRepository repository) {
        this.eventConsumer = eventConsumer;
        this.repository = repository;
    }

    @RabbitListener(queues = AccountsMessagingConfig.SALES_REP_REPLICA_QUEUE)
    public void onSalesRepEvent(Message message) {
        eventConsumer.consume(message, AccountsMessagingConfig.SALES_REP_REPLICA_QUEUE, envelope -> {
            SalesRepEventPayload payload = eventConsumer.payload(envelope, SalesRepEventPayload.class);
            boolean applied = repository.upsert(new SalesRepRef(payload.id(), payload.name(), payload.email(),
                    payload.active(), payload.archived()), envelope.occurredAt());
            log.info("Replica de vendedor #{} {} a partir de {}", payload.id(),
                    applied ? "atualizada" : "mantida (evento mais antigo)", envelope.eventType());
        });
    }
}
