package com.pb.crm.accounts.application.contact;

import com.pb.crm.accounts.application.contact.dto.ContactRequest;
import com.pb.crm.accounts.application.contact.dto.ContactResponse;
import com.pb.crm.accounts.domain.contact.ContactCriteria;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;

import java.util.List;

public interface ContactService {

    ContactResponse create(ContactRequest request);

    ContactResponse update(Long id, ContactRequest request);

    ContactResponse findById(Long id);

    PageResult<ContactResponse> search(ContactCriteria criteria, PageQuery page);

    ContactResponse makePrimary(Long id);

    ContactResponse archive(Long id);

    ContactResponse restore(Long id);

    List<AuditRevision<ContactResponse>> findRevisions(Long id);
}
