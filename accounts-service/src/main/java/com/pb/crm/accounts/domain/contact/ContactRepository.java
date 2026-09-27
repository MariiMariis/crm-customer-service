package com.pb.crm.accounts.domain.contact;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;

import java.util.List;
import java.util.Optional;

public interface ContactRepository {

    Contact save(Contact contact);

    Optional<Contact> findById(Long id);

    PageResult<Contact> search(ContactCriteria criteria, PageQuery page);

    List<Contact> findUnarchivedByCompany(Long companyId);

    Optional<Contact> findPrimaryByCompany(Long companyId);

    long countUnarchivedByCompany(Long companyId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<AuditRevision<Contact>> findRevisions(Long id);
}
