package com.pb.crm.accounts.domain.company;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CompanyRepository {

    Company save(Company company);

    Optional<Company> findById(Long id);

    Map<Long, Company> findAllByIds(Collection<Long> ids);

    PageResult<Company> search(CompanyCriteria criteria, PageQuery page);

    boolean existsByCnpj(Cnpj cnpj);

    boolean existsByCnpjAndIdNot(Cnpj cnpj, Long id);

    List<AuditRevision<Company>> findRevisions(Long id);
}
