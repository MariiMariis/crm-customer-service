package com.pb.crm.accounts.application.contact;

import com.pb.crm.accounts.application.contact.dto.ContactRequest;
import com.pb.crm.accounts.application.contact.dto.ContactResponse;
import com.pb.crm.accounts.application.events.AccountsEventRecorder;
import com.pb.crm.accounts.domain.company.Company;
import com.pb.crm.accounts.domain.company.CompanyRepository;
import com.pb.crm.accounts.domain.contact.Contact;
import com.pb.crm.accounts.domain.contact.ContactCriteria;
import com.pb.crm.accounts.domain.contact.ContactProfile;
import com.pb.crm.accounts.domain.contact.ContactRepository;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.commons.error.ResourceNotFoundException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final AccountsEventRecorder recorder;

    public ContactServiceImpl(ContactRepository contactRepository,
                              CompanyRepository companyRepository,
                              AccountsEventRecorder recorder) {
        this.contactRepository = contactRepository;
        this.companyRepository = companyRepository;
        this.recorder = recorder;
    }

    @Override
    @Transactional
    public ContactResponse create(ContactRequest request) {
        Company company = loadCompany(request.companyId());
        if (contactRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("ja existe um contato cadastrado com este email");
        }
        boolean hasPrimary = contactRepository.findPrimaryByCompany(company.getId()).isPresent();
        boolean wantsPrimary = Boolean.TRUE.equals(request.primary());
        if (wantsPrimary) {
            releaseCurrentPrimary(company.getId());
        }
        Contact contact = Contact.register(company, toProfile(request), wantsPrimary || !hasPrimary);
        return ContactResponse.from(recorder.save(contact), company.displayName());
    }

    @Override
    @Transactional
    public ContactResponse update(Long id, ContactRequest request) {
        Contact contact = load(id);
        assertVersion(contact, request.version());
        if (!contact.getCompanyId().equals(request.companyId())) {
            throw new BusinessRuleException("um contato nao pode ser transferido para outra empresa; cadastre um novo contato");
        }
        if (contactRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new BusinessRuleException("ja existe outro contato cadastrado com este email");
        }
        boolean active = request.active() == null || request.active();
        contact.update(toProfile(request), active);
        Contact saved = recorder.save(contact);
        if (Boolean.TRUE.equals(request.primary()) && !saved.isPrimary()) {
            saved = promote(saved);
        }
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponse findById(Long id) {
        return toResponse(load(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ContactResponse> search(ContactCriteria criteria, PageQuery page) {
        PageResult<Contact> result = contactRepository.search(criteria, page);
        Set<Long> companyIds = result.content().stream().map(Contact::getCompanyId).collect(Collectors.toSet());
        Map<Long, Company> companies = companyRepository.findAllByIds(companyIds);
        return result.map(contact -> {
            Company company = companies.get(contact.getCompanyId());
            return ContactResponse.from(contact, company == null ? null : company.displayName());
        });
    }

    @Override
    @Transactional
    public ContactResponse makePrimary(Long id) {
        return toResponse(promote(load(id)));
    }

    @Override
    @Transactional
    public ContactResponse archive(Long id) {
        Contact contact = load(id);
        contact.archive();
        return toResponse(recorder.save(contact));
    }

    @Override
    @Transactional
    public ContactResponse restore(Long id) {
        Contact contact = load(id);
        Company company = loadCompany(contact.getCompanyId());
        if (company.isArchived()) {
            throw new BusinessRuleException("restaure a empresa antes de restaurar os seus contatos");
        }
        contact.restore();
        return toResponse(recorder.save(contact));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRevision<ContactResponse>> findRevisions(Long id) {
        load(id);
        return contactRepository.findRevisions(id).stream()
                .map(revision -> revision.map(ContactResponse::from))
                .toList();
    }

    private Contact promote(Contact contact) {
        contact.makePrimary();
        releaseCurrentPrimary(contact.getCompanyId());
        return recorder.save(contact);
    }

    private void releaseCurrentPrimary(Long companyId) {
        contactRepository.findPrimaryByCompany(companyId).ifPresent(current -> {
            current.unmarkPrimary();
            recorder.save(current);
        });
    }

    private Contact load(Long id) {
        return contactRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Contato", id));
    }

    private Company loadCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Empresa", companyId));
    }

    private ContactResponse toResponse(Contact contact) {
        String companyName = companyRepository.findById(contact.getCompanyId()).map(Company::displayName).orElse(null);
        return ContactResponse.from(contact, companyName);
    }

    private static ContactProfile toProfile(ContactRequest request) {
        return new ContactProfile(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.mobile(),
                request.jobTitle(),
                request.department(),
                request.decisionRole()
        );
    }

    private static void assertVersion(Contact contact, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(contact.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Contact.class, contact.getId());
        }
    }
}
