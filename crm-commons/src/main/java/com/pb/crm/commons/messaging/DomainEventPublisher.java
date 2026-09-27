package com.pb.crm.commons.messaging;

public interface DomainEventPublisher {

    void publish(String eventType, String aggregateType, Object aggregateId, Object payload);
}
