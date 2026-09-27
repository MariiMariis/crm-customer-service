package com.pb.crm.accounts.application.events;

import com.pb.crm.accounts.domain.company.Company;
import com.pb.crm.accounts.domain.company.CompanyRepository;
import com.pb.crm.accounts.domain.contact.Contact;
import com.pb.crm.accounts.domain.contact.ContactRepository;
import com.pb.crm.commons.messaging.DomainEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountsEventRecorder {

    private static final String EVENT_PREFIX = "accounts.";

    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final DomainEventPublisher eventPublisher;

    public AccountsEventRecorder(CompanyRepository companyRepository,
                                 ContactRepository contactRepository,
                                 DomainEventPublisher eventPublisher) {
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.eventPublisher = eventPublisher;
    }

    public Company save(Company company) {
        List<String> events = company.pullEvents();
        Company saved = companyRepository.save(company);
        CompanySnapshot snapshot = CompanySnapshot.from(saved);
        events.forEach(event -> eventPublisher.publish(EVENT_PREFIX + event, "Company", saved.getId(), snapshot));
        return saved;
    }

    public Contact save(Contact contact) {
        List<String> events = contact.pullEvents();
        Contact saved = contactRepository.save(contact);
        ContactSnapshot snapshot = ContactSnapshot.from(saved);
        events.forEach(event -> eventPublisher.publish(EVENT_PREFIX + event, "Contact", saved.getId(), snapshot));
        return saved;
    }

    public void publish(String eventType, String aggregateType, Object aggregateId, Object payload) {
        eventPublisher.publish(EVENT_PREFIX + eventType, aggregateType, aggregateId, payload);
    }
}
