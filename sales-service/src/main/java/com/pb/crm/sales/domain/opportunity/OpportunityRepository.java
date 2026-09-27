package com.pb.crm.sales.domain.opportunity;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;

import java.util.List;
import java.util.Optional;

public interface OpportunityRepository {

    Opportunity save(Opportunity opportunity);

    Optional<Opportunity> findById(Long id);

    PageResult<Opportunity> search(OpportunityCriteria criteria, PageQuery page);

    List<Opportunity> findAllMatching(OpportunityCriteria criteria);

    List<AuditRevision<Opportunity>> findRevisions(Long id);
}
