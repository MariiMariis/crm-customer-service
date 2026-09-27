package com.pb.crm.accounts.application.company;

import com.pb.crm.accounts.application.company.dto.CompanyRequest;
import com.pb.crm.accounts.application.company.dto.CompanyResponse;
import com.pb.crm.accounts.domain.company.CompanyCriteria;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;

import java.util.List;

public interface CompanyService {

    CompanyResponse create(CompanyRequest request);

    CompanyResponse update(Long id, CompanyRequest request);

    CompanyResponse findById(Long id);

    PageResult<CompanyResponse> search(CompanyCriteria criteria, PageQuery page);

    CompanyResponse archive(Long id);

    CompanyResponse restore(Long id);

    List<AuditRevision<CompanyResponse>> findRevisions(Long id);
}
