package com.pb.crm.sales.application.lead;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.sales.application.lead.dto.ConvertLeadRequest;
import com.pb.crm.sales.application.lead.dto.LeadRequest;
import com.pb.crm.sales.application.lead.dto.LeadResponse;
import com.pb.crm.sales.application.lead.dto.LeadStatsResponse;
import com.pb.crm.sales.domain.lead.LeadCriteria;

import java.util.List;

public interface LeadService {

    LeadResponse create(LeadRequest request);

    LeadResponse update(Long id, LeadRequest request);

    LeadResponse findById(Long id);

    PageResult<LeadResponse> search(LeadCriteria criteria, PageQuery page);

    LeadStatsResponse stats();

    LeadResponse assign(Long id, Long ownerId);

    LeadResponse markContacted(Long id);

    LeadResponse qualify(Long id);

    LeadResponse disqualify(Long id, String reason);

    LeadResponse reopen(Long id);

    LeadResponse requestConversion(Long id, ConvertLeadRequest request);

    LeadResponse completeConversion(Long id, Long companyId, Long contactId);

    LeadResponse failConversion(Long id, String reason);

    LeadResponse archive(Long id);

    LeadResponse restore(Long id);

    List<AuditRevision<LeadResponse>> findRevisions(Long id);
}
