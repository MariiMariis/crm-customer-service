package com.pb.crm.sales.domain.lead;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LeadRepository {

    Lead save(Lead lead);

    Optional<Lead> findById(Long id);

    PageResult<Lead> search(LeadCriteria criteria, PageQuery page);

    boolean existsOpenWithEmail(String email, Long excludedId);

    Map<LeadStatus, Long> countByStatus();

    List<Lead> findConvertingRequestedBefore(Instant requestedBefore);

    List<AuditRevision<Lead>> findRevisions(Long id);
}
