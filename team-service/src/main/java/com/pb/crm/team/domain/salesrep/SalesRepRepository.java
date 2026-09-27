package com.pb.crm.team.domain.salesrep;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;

import java.util.List;
import java.util.Optional;

public interface SalesRepRepository {

    SalesRep save(SalesRep salesRep);

    Optional<SalesRep> findById(Long id);

    PageResult<SalesRep> search(SalesRepCriteria criteria, PageQuery page);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean hasActiveSubordinates(Long managerId);

    List<AuditRevision<SalesRep>> findRevisions(Long id);
}
