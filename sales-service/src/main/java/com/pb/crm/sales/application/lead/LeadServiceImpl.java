package com.pb.crm.sales.application.lead;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.sales.application.lead.dto.ConvertLeadRequest;
import com.pb.crm.sales.application.lead.dto.LeadRequest;
import com.pb.crm.sales.application.lead.dto.LeadResponse;
import com.pb.crm.sales.application.lead.dto.LeadStatsResponse;
import com.pb.crm.sales.application.opportunity.OpportunityService;
import com.pb.crm.sales.domain.lead.ConversionRequest;
import com.pb.crm.sales.domain.lead.Lead;
import com.pb.crm.sales.domain.lead.LeadCriteria;
import com.pb.crm.sales.domain.lead.LeadDetails;
import com.pb.crm.sales.domain.lead.LeadRepository;
import com.pb.crm.sales.domain.lead.LeadStatus;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import com.pb.crm.sales.domain.reference.SalesRepRefRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final SalesRepRefRepository salesRepRefRepository;
    private final OpportunityService opportunityService;

    public LeadServiceImpl(LeadRepository leadRepository,
                           SalesRepRefRepository salesRepRefRepository,
                           OpportunityService opportunityService) {
        this.leadRepository = leadRepository;
        this.salesRepRefRepository = salesRepRefRepository;
        this.opportunityService = opportunityService;
    }

    @Override
    @Transactional
    public LeadResponse create(LeadRequest request) {
        LeadDetails details = toDetails(request);
        assertEmailAvailable(details.email(), null);
        SalesRepRef owner = request.ownerId() == null ? null : resolveOwner(request.ownerId());
        return toDetail(leadRepository.save(Lead.capture(details, owner)));
    }

    @Override
    @Transactional
    public LeadResponse update(Long id, LeadRequest request) {
        Lead lead = load(id);
        if (request.version() != null && !request.version().equals(lead.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Lead.class, id);
        }
        LeadDetails details = toDetails(request);
        assertEmailAvailable(details.email(), id);
        lead.update(details);
        if (request.ownerId() != null && !request.ownerId().equals(lead.getOwnerId())) {
            lead.assignTo(resolveOwner(request.ownerId()));
        }
        return toDetail(leadRepository.save(lead));
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse findById(Long id) {
        return toDetail(load(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<LeadResponse> search(LeadCriteria criteria, PageQuery page) {
        PageResult<Lead> result = leadRepository.search(criteria, page);
        Set<Long> ownerIds = result.content().stream()
                .map(Lead::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SalesRepRef> owners = salesRepRefRepository.findAllByIds(ownerIds);
        return result.map(lead -> LeadResponse.from(lead, owners.get(lead.getOwnerId()), false));
    }

    @Override
    @Transactional(readOnly = true)
    public LeadStatsResponse stats() {
        Map<LeadStatus, Long> byStatus = leadRepository.countByStatus();
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        long open = LeadStatus.OPEN.stream().mapToLong(status -> byStatus.getOrDefault(status, 0L)).sum();
        return new LeadStatsResponse(total, open, byStatus);
    }

    @Override
    @Transactional
    public LeadResponse assign(Long id, Long ownerId) {
        Lead lead = load(id);
        lead.assignTo(resolveOwner(ownerId));
        return toDetail(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse markContacted(Long id) {
        Lead lead = load(id);
        lead.markContacted();
        return toDetail(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse qualify(Long id) {
        Lead lead = load(id);
        lead.qualify();
        return toDetail(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse disqualify(Long id, String reason) {
        Lead lead = load(id);
        lead.disqualify(reason);
        return toDetail(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse reopen(Long id) {
        Lead lead = load(id);
        if (lead.getDetails().email() != null) {
            assertEmailAvailable(lead.getDetails().email(), id);
        }
        lead.reopen();
        return toDetail(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse requestConversion(Long id, ConvertLeadRequest request) {
        Lead lead = load(id);
        lead.requestConversion(new ConversionRequest(
                request.cnpj(),
                request.industry(),
                request.companySize(),
                request.city(),
                request.state(),
                request.createOpportunity(),
                request.opportunityTitle(),
                request.expectedCloseDate(),
                null
        ));
        return toDetail(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse completeConversion(Long id, Long companyId, Long contactId) {
        Lead lead = load(id);
        if (lead.getStatus() != LeadStatus.CONVERTING) {
            throw new BusinessRuleException("o lead nao esta em conversao");
        }
        Long opportunityId = lead.getConversion().createOpportunity()
                ? opportunityService.openFromLead(lead, companyId, contactId)
                : null;
        lead.completeConversion(companyId, contactId, opportunityId);
        return toDetail(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse failConversion(Long id, String reason) {
        Lead lead = load(id);
        lead.failConversion(reason);
        return toDetail(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse archive(Long id) {
        Lead lead = load(id);
        lead.archive();
        return toDetail(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse restore(Long id) {
        Lead lead = load(id);
        if (lead.getStatus().isOpen() && lead.getDetails().email() != null) {
            assertEmailAvailable(lead.getDetails().email(), id);
        }
        lead.restore();
        return toDetail(leadRepository.save(lead));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRevision<LeadResponse>> findRevisions(Long id) {
        load(id);
        return leadRepository.findRevisions(id).stream()
                .map(revision -> revision.map(lead -> LeadResponse.from(lead, null, false)))
                .toList();
    }

    private Lead load(Long id) {
        return leadRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Lead", id));
    }

    private SalesRepRef resolveOwner(Long ownerId) {
        return salesRepRefRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessRuleException(
                        "vendedor responsavel %d nao encontrado na base sincronizada da equipe comercial".formatted(ownerId)));
    }

    private void assertEmailAvailable(String email, Long excludedId) {
        if (email != null && !email.isBlank() && leadRepository.existsOpenWithEmail(email, excludedId)) {
            throw new BusinessRuleException("ja existe um lead aberto com este email");
        }
    }

    private LeadResponse toDetail(Lead lead) {
        SalesRepRef owner = lead.getOwnerId() == null ? null : salesRepRefRepository.findById(lead.getOwnerId()).orElse(null);
        return LeadResponse.from(lead, owner, true);
    }

    private static LeadDetails toDetails(LeadRequest request) {
        return new LeadDetails(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.companyName(),
                request.jobTitle(),
                request.source(),
                request.estimatedValue(),
                request.notes()
        );
    }
}
