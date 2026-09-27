package com.pb.crm.sales.infrastructure.messaging;

import com.pb.crm.commons.messaging.consumer.EventConsumer;
import com.pb.crm.sales.domain.reference.ProductRef;
import com.pb.crm.sales.domain.reference.ReferenceRepository;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import com.pb.crm.sales.domain.reference.SalesRepRefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReplicaListeners {

    private static final Logger log = LoggerFactory.getLogger(ReplicaListeners.class);

    private final EventConsumer eventConsumer;
    private final SalesRepRefRepository salesRepRefRepository;
    private final ReferenceRepository referenceRepository;

    public ReplicaListeners(EventConsumer eventConsumer,
                            SalesRepRefRepository salesRepRefRepository,
                            ReferenceRepository referenceRepository) {
        this.eventConsumer = eventConsumer;
        this.salesRepRefRepository = salesRepRefRepository;
        this.referenceRepository = referenceRepository;
    }

    @RabbitListener(queues = SalesMessagingConfig.SALES_REP_REPLICA_QUEUE)
    public void onSalesRepEvent(Message message) {
        eventConsumer.consume(message, SalesMessagingConfig.SALES_REP_REPLICA_QUEUE, envelope -> {
            ReplicaPayloads.SalesRep payload = eventConsumer.payload(envelope, ReplicaPayloads.SalesRep.class);
            boolean applied = salesRepRefRepository.upsert(new SalesRepRef(payload.id(), payload.name(), payload.email(),
                    payload.managerId(), payload.active(), payload.archived()), envelope.occurredAt());
            log.info("Replica de vendedor #{} {} a partir de {}", payload.id(),
                    applied ? "atualizada" : "mantida (evento mais antigo)", envelope.eventType());
        });
    }

    @RabbitListener(queues = SalesMessagingConfig.PRODUCT_REPLICA_QUEUE)
    public void onProductEvent(Message message) {
        eventConsumer.consume(message, SalesMessagingConfig.PRODUCT_REPLICA_QUEUE, envelope -> {
            ReplicaPayloads.Product payload = eventConsumer.payload(envelope, ReplicaPayloads.Product.class);
            boolean applied = referenceRepository.upsertProduct(new ProductRef(payload.id(), payload.sku(), payload.name(),
                    payload.category(), payload.billing(), payload.unitPrice(), payload.maxDiscountPercent(),
                    payload.active(), payload.archived()), envelope.occurredAt());
            log.info("Replica do produto {} {} a partir de {}", payload.sku(),
                    applied ? "atualizada" : "mantida (evento mais antigo)", envelope.eventType());
        });
    }
}
