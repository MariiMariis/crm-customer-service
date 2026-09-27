package com.pb.crm.accounts.infrastructure.persistence;

import com.pb.crm.accounts.domain.contact.Contact;
import com.pb.crm.accounts.domain.contact.ContactCriteria;
import com.pb.crm.accounts.domain.contact.ContactRepository;
import com.pb.crm.commons.audit.AuditRevisions;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ContactRepositoryAdapter implements ContactRepository {

    private final SpringDataContactRepository jpaRepository;
    private final AccountsMapper mapper;

    public ContactRepositoryAdapter(SpringDataContactRepository jpaRepository, AccountsMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Contact save(Contact contact) {
        ContactJpaEntity entity = contact.isNew()
                ? new ContactJpaEntity()
                : jpaRepository.findById(contact.getId())
                        .orElseThrow(() -> ResourceNotFoundException.forId("Contato", contact.getId()));
        mapper.copyToEntity(contact, entity);
        return mapper.toDomain(jpaRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<Contact> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Contact> search(ContactCriteria criteria, PageQuery page) {
        Sort sort = Sort.by("firstName").ascending().and(Sort.by("lastName")).and(Sort.by("id"));
        Page<ContactJpaEntity> result = jpaRepository.findAll(
                AccountsSpecifications.contacts(criteria), PageRequest.of(page.page(), page.size(), sort));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    @Override
    public List<Contact> findUnarchivedByCompany(Long companyId) {
        return jpaRepository.findByCompanyIdAndArchivedFalseOrderByFirstNameAscLastNameAsc(companyId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Contact> findPrimaryByCompany(Long companyId) {
        return jpaRepository.findFirstByCompanyIdAndPrimaryTrueAndArchivedFalse(companyId).map(mapper::toDomain);
    }

    @Override
    public long countUnarchivedByCompany(Long companyId) {
        return jpaRepository.countByCompanyIdAndArchivedFalse(companyId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return jpaRepository.existsByEmailIgnoreCaseAndIdNot(email.trim(), id);
    }

    @Override
    public List<AuditRevision<Contact>> findRevisions(Long id) {
        return jpaRepository.findRevisions(id).stream()
                .map(revision -> AuditRevisions.from(revision, mapper::toDomain))
                .toList();
    }
}
