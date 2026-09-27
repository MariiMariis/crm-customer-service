package com.pb.crm.sales.infrastructure.messaging;

import com.pb.crm.commons.messaging.consumer.EventConsumer;
import com.pb.crm.commons.messaging.consumer.NonRetryableEventException;
import com.pb.crm.sales.application.lead.LeadService;
import com.pb.crm.sales.domain.reference.CompanyRef;
import com.pb.crm.sales.domain.reference.ContactRef;
import com.pb.crm.sales.domain.reference.ReferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AccountsListeners {

    private static final Logger log = LoggerFactory.getLogger(AccountsListeners.class);

    private final EventConsumer eventConsumer;
    private final ReferenceRepository referenceRepository;
    private final LeadService leadService;

    public AccountsListeners(EventConsumer eventConsumer, ReferenceRepository referenceRepository, LeadService leadService) {
        this.eventConsumer = eventConsumer;
        this.referenceRepository = referenceRepository;
        this.leadService = leadService;
    }

    @RabbitListener(queues = SalesMessagingConfig.COMPANY_REPLICA_QUEUE)
    public void onCompanyEvent(Message message) {
        eventConsumer.consume(message, SalesMessagingConfig.COMPANY_REPLICA_QUEUE, envelope -> {
            AccountsPayloads.Company payload = eventConsumer.payload(envelope, AccountsPayloads.Company.class);
            boolean applied = referenceRepository.upsertCompany(new CompanyRef(payload.id(), payload.displayName(),
                    payload.cnpj(), payload.ownerId(), payload.archived()), envelope.occurredAt());
            log.info("Replica da empresa #{} {} a partir de {}", payload.id(),
                    applied ? "atualizada" : "mantida (evento mais antigo)", envelope.eventType());
        });
    }

    @RabbitListener(queues = SalesMessagingConfig.CONTACT_REPLICA_QUEUE)
    public void onContactEvent(Message message) {
        eventConsumer.consume(message, SalesMessagingConfig.CONTACT_REPLICA_QUEUE, envelope -> {
            AccountsPayloads.Contact payload = eventConsumer.payload(envelope, AccountsPayloads.Contact.class);
            boolean applied = referenceRepository.upsertContact(new ContactRef(payload.id(), payload.companyId(),
                    payload.fullName(), payload.email(), payload.active(), payload.archived()), envelope.occurredAt());
            log.info("Replica do contato #{} {} a partir de {}", payload.id(),
                    applied ? "atualizada" : "mantida (evento mais antigo)", envelope.eventType());
        });
    }

    @RabbitListener(queues = SalesMessagingConfig.LEAD_CONVERSION_REPLY_QUEUE)
    public void onLeadAccountReply(Message message) {
        eventConsumer.consume(message, SalesMessagingConfig.LEAD_CONVERSION_REPLY_QUEUE, envelope -> {
            switch (envelope.eventType()) {
                case "accounts.lead-account.provisioned" -> {
                    AccountsPayloads.LeadAccountProvisioned payload =
                            eventConsumer.payload(envelope, AccountsPayloads.LeadAccountProvisioned.class);
                    referenceRepository.upsertCompany(new CompanyRef(payload.companyId(), payload.companyName(),
                            payload.cnpj(), payload.companyOwnerId(), false), envelope.occurredAt());
                    referenceRepository.upsertContact(new ContactRef(payload.contactId(), payload.companyId(),
                            payload.contactName(), payload.contactEmail(), true, false), envelope.occurredAt());
                    leadService.applyProvisionedAccount(payload.leadId(), payload.companyId(), payload.contactId());
                }
                case "accounts.lead-account.rejected" -> {
                    AccountsPayloads.LeadAccountRejected payload =
                            eventConsumer.payload(envelope, AccountsPayloads.LeadAccountRejected.class);
                    leadService.applyRejectedAccount(payload.leadId(), payload.reason());
                }
                default -> throw new NonRetryableEventException("resposta de saga desconhecida: " + envelope.eventType());
            }
        });
    }
}
