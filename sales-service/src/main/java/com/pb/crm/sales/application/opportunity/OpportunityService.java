package com.pb.crm.sales.application.opportunity;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.sales.application.opportunity.dto.OpportunityItemRequest;
import com.pb.crm.sales.application.opportunity.dto.OpportunityRequest;
import com.pb.crm.sales.application.opportunity.dto.OpportunityResponse;
import com.pb.crm.sales.application.opportunity.dto.PipelineResponse;
import com.pb.crm.sales.domain.lead.Lead;
import com.pb.crm.sales.domain.opportunity.OpportunityCriteria;
import com.pb.crm.sales.domain.opportunity.OpportunityStage;

import java.util.List;

public interface OpportunityService {

    OpportunityResponse create(OpportunityRequest request);

    Long openFromLead(Lead lead, Long companyId, Long contactId);

    OpportunityResponse update(Long id, OpportunityRequest request);

    OpportunityResponse findById(Long id);

    PageResult<OpportunityResponse> search(OpportunityCriteria criteria, PageQuery page);

    PipelineResponse pipeline(Long ownerId);

    OpportunityResponse addItem(Long id, OpportunityItemRequest request);

    OpportunityResponse changeItem(Long id, Long itemId, OpportunityItemRequest request);

    OpportunityResponse removeItem(Long id, Long itemId);

    OpportunityResponse moveTo(Long id, OpportunityStage stage);

    OpportunityResponse adjustProbability(Long id, int probability);

    OpportunityResponse win(Long id);

    OpportunityResponse lose(Long id, String reason);

    OpportunityResponse reopen(Long id);

    OpportunityResponse decideDiscount(Long id, Long approverId, boolean approved, String comment);

    OpportunityResponse archive(Long id);

    OpportunityResponse restore(Long id);

    List<AuditRevision<OpportunityResponse>> findRevisions(Long id);
}
