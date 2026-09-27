package com.pb.crm.accounts.application.conversion;

import com.pb.crm.accounts.application.events.AccountsEventRecorder;
import com.pb.crm.accounts.domain.company.BrazilianState;
import com.pb.crm.accounts.domain.company.Cnpj;
import com.pb.crm.accounts.domain.company.Company;
import com.pb.crm.accounts.domain.company.CompanyProfile;
import com.pb.crm.accounts.domain.company.CompanyRepository;
import com.pb.crm.accounts.domain.company.CompanySize;
import com.pb.crm.accounts.domain.company.CompanyType;
import com.pb.crm.accounts.domain.company.Industry;
import com.pb.crm.accounts.domain.contact.Contact;
import com.pb.crm.accounts.domain.contact.ContactProfile;
import com.pb.crm.accounts.domain.contact.ContactRepository;
import com.pb.crm.accounts.domain.contact.DecisionRole;
import com.pb.crm.accounts.domain.salesrep.SalesRepRef;
import com.pb.crm.accounts.domain.salesrep.SalesRepRefRepository;
import com.pb.crm.commons.error.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LeadAccountProvisioner {

    private static final Logger log = LoggerFactory.getLogger(LeadAccountProvisioner.class);
    private static final String AGGREGATE_TYPE = "Lead";

    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final SalesRepRefRepository salesRepRefRepository;
    private final AccountsEventRecorder recorder;

    public LeadAccountProvisioner(CompanyRepository companyRepository,
                                  ContactRepository contactRepository,
                                  SalesRepRefRepository salesRepRefRepository,
                                  AccountsEventRecorder recorder) {
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.salesRepRefRepository = salesRepRefRepository;
        this.recorder = recorder;
    }

    @Transactional
    public void provision(LeadAccountRequest request) {
        try {
            LeadAccountOutcome.Provisioned provisioned = provisionAccount(request);
            recorder.publish(LeadAccountOutcome.PROVISIONED, AGGREGATE_TYPE, request.leadId(), provisioned);
            log.info("Conta provisionada para o lead #{}: empresa #{} ({}), contato #{} ({})", request.leadId(),
                    provisioned.companyId(), provisioned.companyCreated() ? "nova" : "existente",
                    provisioned.contactId(), provisioned.contactCreated() ? "novo" : "existente");
        } catch (BusinessRuleException | IllegalArgumentException ex) {
            recorder.publish(LeadAccountOutcome.REJECTED, AGGREGATE_TYPE, request.leadId(),
                    new LeadAccountOutcome.Rejected(request.leadId(), ex.getMessage()));
            log.warn("Conversao do lead #{} rejeitada pelo servico de contas: {}", request.leadId(), ex.getMessage());
        }
    }

    private LeadAccountOutcome.Provisioned provisionAccount(LeadAccountRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new BusinessRuleException("o lead precisa ter e-mail para virar contato");
        }
        Cnpj cnpj = Cnpj.of(request.cnpj());
        Industry industry = parse(Industry.class, request.industry(), "segmento");
        CompanySize size = parse(CompanySize.class, request.companySize(), "porte");
        BrazilianState state = request.state() == null ? null : parse(BrazilianState.class, request.state(), "UF");

        Optional<Company> existingCompany = companyRepository.findByCnpj(cnpj);
        if (existingCompany.isPresent() && existingCompany.get().isArchived()) {
            throw new BusinessRuleException("a empresa com o CNPJ %s esta arquivada; restaure-a antes de converter o lead"
                    .formatted(cnpj.formatted()));
        }
        Optional<Contact> existingContact = contactRepository.findByEmail(request.email());
        if (existingContact.isPresent()) {
            Contact contact = existingContact.get();
            if (existingCompany.isEmpty() || !contact.getCompanyId().equals(existingCompany.get().getId())) {
                throw new BusinessRuleException("o e-mail %s ja pertence a um contato de outra empresa".formatted(request.email()));
            }
            if (contact.isArchived()) {
                throw new BusinessRuleException("o contato com o e-mail %s esta arquivado".formatted(request.email()));
            }
        }

        boolean companyCreated = existingCompany.isEmpty();
        Company company = existingCompany.orElseGet(() -> recorder.save(Company.register(
                new CompanyProfile(request.companyName(), null, cnpj, industry, size, null, null, null, null,
                        request.city(), state, CompanyType.PROSPECT, "Empresa criada a partir do lead #" + request.leadId()),
                resolveOwner(request.ownerId()))));

        boolean contactCreated = existingContact.isEmpty();
        boolean primary = contactRepository.findPrimaryByCompany(company.getId()).isEmpty();
        Contact contact = existingContact.orElseGet(() -> recorder.save(Contact.register(company,
                new ContactProfile(request.firstName(), request.lastName(), request.email(), request.phone(), null,
                        request.jobTitle(), null, DecisionRole.CHAMPION),
                primary)));

        return new LeadAccountOutcome.Provisioned(
                request.leadId(),
                company.getId(),
                company.displayName(),
                company.getProfile().cnpj().value(),
                company.getOwnerId(),
                companyCreated,
                contact.getId(),
                contact.fullName(),
                contact.getProfile().email(),
                contactCreated
        );
    }

    private SalesRepRef resolveOwner(Long ownerId) {
        if (ownerId == null) {
            return null;
        }
        return salesRepRefRepository.findById(ownerId).filter(SalesRepRef::canOwnRecords).orElse(null);
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value, String field) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("%s invalido: %s".formatted(field, value));
        }
    }
}
