package com.pb.crm.team.application.salesrep;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.team.application.salesrep.dto.SalesRepRequest;
import com.pb.crm.team.application.salesrep.dto.SalesRepResponse;
import com.pb.crm.team.domain.salesrep.SalesRepCriteria;

import java.util.List;

public interface SalesRepService {

    SalesRepResponse create(SalesRepRequest request);

    SalesRepResponse update(Long id, SalesRepRequest request);

    SalesRepResponse findById(Long id);

    PageResult<SalesRepResponse> search(SalesRepCriteria criteria, PageQuery page);

    SalesRepResponse activate(Long id);

    SalesRepResponse deactivate(Long id);

    SalesRepResponse archive(Long id);

    SalesRepResponse restore(Long id);

    List<AuditRevision<SalesRepResponse>> findRevisions(Long id);
}
