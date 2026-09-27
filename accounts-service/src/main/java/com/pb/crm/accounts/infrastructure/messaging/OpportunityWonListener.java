package com.pb.crm.accounts.infrastructure.messaging;

import com.pb.crm.accounts.application.conversion.CustomerLifecycleService;
import com.pb.crm.commons.messaging.consumer.EventConsumer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OpportunityWonListener {

    public record OpportunityWonPayload(Long id, Long companyId) {
    }

    private final EventConsumer eventConsumer;
    private final CustomerLifecycleService customerLifecycleService;

    public OpportunityWonListener(EventConsumer eventConsumer, CustomerLifecycleService customerLifecycleService) {
        this.eventConsumer = eventConsumer;
        this.customerLifecycleService = customerLifecycleService;
    }

    @RabbitListener(queues = AccountsMessagingConfig.CUSTOMER_LIFECYCLE_QUEUE)
    public void onOpportunityWon(Message message) {
        eventConsumer.consume(message, AccountsMessagingConfig.CUSTOMER_LIFECYCLE_QUEUE, envelope -> {
            OpportunityWonPayload payload = eventConsumer.payload(envelope, OpportunityWonPayload.class);
            customerLifecycleService.onOpportunityWon(payload.companyId(), payload.id());
        });
    }
}
