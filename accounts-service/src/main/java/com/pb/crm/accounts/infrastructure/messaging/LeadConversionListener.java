package com.pb.crm.accounts.infrastructure.messaging;

import com.pb.crm.accounts.application.conversion.LeadAccountProvisioner;
import com.pb.crm.accounts.application.conversion.LeadAccountRequest;
import com.pb.crm.commons.messaging.consumer.EventConsumer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class LeadConversionListener {

    private final EventConsumer eventConsumer;
    private final LeadAccountProvisioner provisioner;

    public LeadConversionListener(EventConsumer eventConsumer, LeadAccountProvisioner provisioner) {
        this.eventConsumer = eventConsumer;
        this.provisioner = provisioner;
    }

    @RabbitListener(queues = AccountsMessagingConfig.LEAD_CONVERSION_QUEUE)
    public void onConversionRequested(Message message) {
        eventConsumer.consume(message, AccountsMessagingConfig.LEAD_CONVERSION_QUEUE, envelope -> {
            LeadConversionRequestedPayload payload = eventConsumer.payload(envelope, LeadConversionRequestedPayload.class);
            provisioner.provision(new LeadAccountRequest(
                    payload.leadId(),
                    payload.companyName(),
                    payload.cnpj(),
                    payload.industry(),
                    payload.companySize(),
                    payload.city(),
                    payload.state(),
                    payload.firstName(),
                    payload.lastName(),
                    payload.email(),
                    payload.phone(),
                    payload.jobTitle(),
                    payload.ownerId()
            ));
        });
    }
}
