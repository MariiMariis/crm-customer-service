package com.pb.crm.sales.application.activity;

import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.sales.domain.activity.RelatedTo;
import com.pb.crm.sales.domain.lead.Lead;
import com.pb.crm.sales.domain.lead.LeadRepository;
import com.pb.crm.sales.domain.opportunity.Opportunity;
import com.pb.crm.sales.domain.opportunity.OpportunityRepository;
import com.pb.crm.sales.domain.reference.CompanyRef;
import com.pb.crm.sales.domain.reference.ContactRef;
import com.pb.crm.sales.domain.reference.ReferenceRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RelatedRecordResolver {

    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final ReferenceRepository referenceRepository;

    public RelatedRecordResolver(LeadRepository leadRepository,
                                 OpportunityRepository opportunityRepository,
                                 ReferenceRepository referenceRepository) {
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
        this.referenceRepository = referenceRepository;
    }

    public String requireActive(RelatedTo relatedTo) {
        return switch (relatedTo.type()) {
            case LEAD -> {
                Lead lead = leadRepository.findById(relatedTo.id()).orElseThrow(() -> notFound(relatedTo));
                if (lead.isArchived()) {
                    throw new BusinessRuleException("o lead relacionado esta arquivado");
                }
                yield lead.fullName();
            }
            case OPPORTUNITY -> {
                Opportunity opportunity = opportunityRepository.findById(relatedTo.id()).orElseThrow(() -> notFound(relatedTo));
                if (opportunity.isArchived()) {
                    throw new BusinessRuleException("a oportunidade relacionada esta arquivada");
                }
                yield opportunity.getDetails().title();
            }
            case COMPANY -> {
                CompanyRef company = referenceRepository.findCompany(relatedTo.id()).orElseThrow(() -> notFound(relatedTo));
                if (company.archived()) {
                    throw new BusinessRuleException("a empresa relacionada esta arquivada");
                }
                yield company.displayName();
            }
            case CONTACT -> {
                ContactRef contact = referenceRepository.findContact(relatedTo.id()).orElseThrow(() -> notFound(relatedTo));
                if (!contact.isAvailable()) {
                    throw new BusinessRuleException("o contato relacionado esta inativo ou arquivado");
                }
                yield contact.fullName();
            }
        };
    }

    public String nameOf(RelatedTo relatedTo) {
        Optional<String> name = switch (relatedTo.type()) {
            case LEAD -> leadRepository.findById(relatedTo.id()).map(Lead::fullName);
            case OPPORTUNITY -> opportunityRepository.findById(relatedTo.id()).map(o -> o.getDetails().title());
            case COMPANY -> referenceRepository.findCompany(relatedTo.id()).map(CompanyRef::displayName);
            case CONTACT -> referenceRepository.findContact(relatedTo.id()).map(ContactRef::fullName);
        };
        return name.orElse(null);
    }

    private static BusinessRuleException notFound(RelatedTo relatedTo) {
        return new BusinessRuleException("%s %d nao encontrado(a)".formatted(relatedTo.type(), relatedTo.id()));
    }
}
