package com.pb.crm.accounts.application.company;

import com.pb.crm.accounts.application.company.dto.CompanyRequest;
import com.pb.crm.accounts.application.company.dto.CompanyResponse;
import com.pb.crm.accounts.domain.company.Cnpj;
import com.pb.crm.accounts.domain.company.Company;
import com.pb.crm.accounts.domain.company.CompanyCriteria;
import com.pb.crm.accounts.domain.company.CompanyProfile;
import com.pb.crm.accounts.domain.company.CompanyRepository;
import com.pb.crm.accounts.domain.contact.Contact;
import com.pb.crm.accounts.domain.contact.ContactRepository;
import com.pb.crm.accounts.domain.salesrep.SalesRepRef;
import com.pb.crm.accounts.domain.salesrep.SalesRepRefRepository;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final SalesRepRefRepository salesRepRefRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository,
                              ContactRepository contactRepository,
                              SalesRepRefRepository salesRepRefRepository) {
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.salesRepRefRepository = salesRepRefRepository;
    }

    @Override
    @Transactional
    public CompanyResponse create(CompanyRequest request) {
        CompanyProfile profile = toProfile(request);
        if (companyRepository.existsByCnpj(profile.cnpj())) {
            throw new BusinessRuleException("ja existe uma empresa cadastrada com este CNPJ");
        }
        Company company = Company.register(profile, resolveOwner(request.ownerId()));
        return toDetail(companyRepository.save(company));
    }

    @Override
    @Transactional
    public CompanyResponse update(Long id, CompanyRequest request) {
        Company company = load(id);
        assertVersion(company, request.version());
        CompanyProfile profile = toProfile(request);
        if (companyRepository.existsByCnpjAndIdNot(profile.cnpj(), id)) {
            throw new BusinessRuleException("ja existe outra empresa cadastrada com este CNPJ");
        }
        company.update(profile);
        if (!Objects.equals(request.ownerId(), company.getOwnerId())) {
            company.reassignOwner(resolveOwner(request.ownerId()));
        }
        return toDetail(companyRepository.save(company));
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse findById(Long id) {
        return toDetail(load(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CompanyResponse> search(CompanyCriteria criteria, PageQuery page) {
        PageResult<Company> result = companyRepository.search(criteria, page);
        Set<Long> ownerIds = result.content().stream()
                .map(Company::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SalesRepRef> owners = salesRepRefRepository.findAllByIds(ownerIds);
        return result.map(company -> CompanyResponse.summary(company, owners.get(company.getOwnerId())));
    }

    @Override
    @Transactional
    public CompanyResponse archive(Long id) {
        Company company = load(id);
        company.archive();
        Company saved = companyRepository.save(company);
        for (Contact contact : contactRepository.findUnarchivedByCompany(id)) {
            contact.archive();
            contactRepository.save(contact);
        }
        return toDetail(saved);
    }

    @Override
    @Transactional
    public CompanyResponse restore(Long id) {
        Company company = load(id);
        company.restore();
        return toDetail(companyRepository.save(company));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRevision<CompanyResponse>> findRevisions(Long id) {
        load(id);
        return companyRepository.findRevisions(id).stream()
                .map(revision -> revision.map(company -> CompanyResponse.summary(company, null)))
                .toList();
    }

    private Company load(Long id) {
        return companyRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Empresa", id));
    }

    private SalesRepRef resolveOwner(Long ownerId) {
        if (ownerId == null) {
            return null;
        }
        return salesRepRefRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessRuleException(
                        "vendedor responsavel %d nao encontrado na base sincronizada da equipe comercial".formatted(ownerId)));
    }

    private static CompanyProfile toProfile(CompanyRequest request) {
        return new CompanyProfile(
                request.legalName(),
                request.tradeName(),
                Cnpj.of(request.cnpj()),
                request.industry(),
                request.size(),
                request.employees(),
                request.annualRevenue(),
                request.website(),
                request.phone(),
                request.city(),
                request.state(),
                request.type(),
                request.notes()
        );
    }

    private static void assertVersion(Company company, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(company.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Company.class, company.getId());
        }
    }

    private CompanyResponse toDetail(Company company) {
        SalesRepRef owner = company.getOwnerId() == null
                ? null
                : salesRepRefRepository.findById(company.getOwnerId()).orElse(null);
        long contactCount = contactRepository.countUnarchivedByCompany(company.getId());
        Contact primary = contactRepository.findPrimaryByCompany(company.getId()).orElse(null);
        return CompanyResponse.detail(company, owner, contactCount, primary);
    }
}
