package com.pb.crm.sales.application.opportunity;

import com.pb.crm.commons.actor.RequestActor;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.sales.application.opportunity.dto.OpportunityItemRequest;
import com.pb.crm.sales.application.opportunity.dto.OpportunityRequest;
import com.pb.crm.sales.application.opportunity.dto.OpportunityResponse;
import com.pb.crm.sales.application.opportunity.dto.PipelineResponse;
import com.pb.crm.sales.domain.lead.ConversionRequest;
import com.pb.crm.sales.domain.lead.Lead;
import com.pb.crm.sales.domain.opportunity.DiscountApprovalStatus;
import com.pb.crm.sales.domain.opportunity.Opportunity;
import com.pb.crm.sales.domain.opportunity.OpportunityCriteria;
import com.pb.crm.sales.domain.opportunity.OpportunityDetails;
import com.pb.crm.sales.domain.opportunity.OpportunityRepository;
import com.pb.crm.sales.domain.opportunity.OpportunityStage;
import com.pb.crm.sales.domain.reference.CompanyRef;
import com.pb.crm.sales.domain.reference.ContactRef;
import com.pb.crm.sales.domain.reference.ProductRef;
import com.pb.crm.sales.domain.reference.ReferenceRepository;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import com.pb.crm.sales.domain.reference.SalesRepRefRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OpportunityServiceImpl implements OpportunityService {

    private static final int DEFAULT_TERM_MONTHS = 12;

    private final OpportunityRepository opportunityRepository;
    private final ReferenceRepository referenceRepository;
    private final SalesRepRefRepository salesRepRefRepository;

    public OpportunityServiceImpl(OpportunityRepository opportunityRepository,
                                  ReferenceRepository referenceRepository,
                                  SalesRepRefRepository salesRepRefRepository) {
        this.opportunityRepository = opportunityRepository;
        this.referenceRepository = referenceRepository;
        this.salesRepRefRepository = salesRepRefRepository;
    }

    @Override
    @Transactional
    public OpportunityResponse create(OpportunityRequest request) {
        CompanyRef company = loadCompany(request.companyId());
        ContactRef contact = request.contactId() == null ? null : loadContact(request.contactId());
        Opportunity opportunity = Opportunity.open(toDetails(request), company, contact, loadOwner(request.ownerId()),
                null, RequestActor.current());
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional
    public Long openFromLead(Lead lead, Long companyId, Long contactId) {
        ConversionRequest conversion = lead.getConversion();
        OpportunityDetails details = new OpportunityDetails(
                conversion.opportunityTitle(),
                "Oportunidade originada do lead #%d (%s)".formatted(lead.getId(), lead.fullName()),
                conversion.expectedCloseDate(),
                DEFAULT_TERM_MONTHS,
                lead.getDetails().estimatedValue());
        Opportunity opportunity = Opportunity.open(details, loadCompany(companyId), loadContact(contactId),
                loadOwner(lead.getOwnerId()), lead.getId(), RequestActor.current());
        return opportunityRepository.save(opportunity).getId();
    }

    @Override
    @Transactional
    public OpportunityResponse update(Long id, OpportunityRequest request) {
        Opportunity opportunity = load(id);
        if (request.version() != null && !request.version().equals(opportunity.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Opportunity.class, id);
        }
        if (!opportunity.getCompanyId().equals(request.companyId())) {
            throw new BusinessRuleException("a empresa de uma oportunidade nao pode ser alterada");
        }
        ContactRef contact = request.contactId() == null ? null : loadContact(request.contactId());
        opportunity.updateDetails(toDetails(request), contact);
        if (!opportunity.getOwnerId().equals(request.ownerId())) {
            opportunity.reassign(loadOwner(request.ownerId()));
        }
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional(readOnly = true)
    public OpportunityResponse findById(Long id) {
        return toDetail(load(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<OpportunityResponse> search(OpportunityCriteria criteria, PageQuery page) {
        PageResult<Opportunity> result = opportunityRepository.search(criteria, page);
        Function<Opportunity, OpportunityResponse.Names> names = namesFor(result.content());
        return result.map(opportunity -> OpportunityResponse.from(opportunity, names.apply(opportunity), false));
    }

    @Override
    @Transactional(readOnly = true)
    public PipelineResponse pipeline(Long ownerId) {
        List<Opportunity> all = opportunityRepository.findAllMatching(
                new OpportunityCriteria(null, null, null, null, ownerId, null, null, null, false));
        List<PipelineResponse.StageSummary> stages = new ArrayList<>();
        for (OpportunityStage stage : OpportunityStage.values()) {
            List<Opportunity> inStage = all.stream().filter(o -> o.getStage() == stage).toList();
            stages.add(new PipelineResponse.StageSummary(stage, inStage.size(),
                    sum(inStage, Opportunity::amount), sum(inStage, Opportunity::weightedAmount)));
        }
        List<Opportunity> open = all.stream().filter(o -> o.getStage().isOpen()).toList();
        List<Opportunity> won = all.stream().filter(o -> o.getStage() == OpportunityStage.WON).toList();
        long pendingApprovals = open.stream()
                .filter(o -> o.getDiscountApproval() == DiscountApprovalStatus.PENDING)
                .count();
        return new PipelineResponse(stages, open.size(), sum(open, Opportunity::amount),
                sum(open, Opportunity::weightedAmount), sum(won, Opportunity::amount),
                sum(won, Opportunity::monthlyRecurringValue), pendingApprovals);
    }

    @Override
    @Transactional
    public OpportunityResponse addItem(Long id, OpportunityItemRequest request) {
        Opportunity opportunity = load(id);
        opportunity.addItem(loadProduct(request.productId()), request.quantity(), request.discountPercent());
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional
    public OpportunityResponse changeItem(Long id, Long itemId, OpportunityItemRequest request) {
        Opportunity opportunity = load(id);
        opportunity.changeItem(itemId, request.quantity(), request.discountPercent());
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional
    public OpportunityResponse removeItem(Long id, Long itemId) {
        Opportunity opportunity = load(id);
        opportunity.removeItem(itemId);
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional
    public OpportunityResponse moveTo(Long id, OpportunityStage stage) {
        Opportunity opportunity = load(id);
        opportunity.moveTo(stage, RequestActor.current());
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional
    public OpportunityResponse adjustProbability(Long id, int probability) {
        Opportunity opportunity = load(id);
        opportunity.adjustProbability(probability);
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional
    public OpportunityResponse win(Long id) {
        Opportunity opportunity = load(id);
        opportunity.markWon(RequestActor.current());
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional
    public OpportunityResponse lose(Long id, String reason) {
        Opportunity opportunity = load(id);
        opportunity.markLost(reason, RequestActor.current());
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional
    public OpportunityResponse reopen(Long id) {
        Opportunity opportunity = load(id);
        opportunity.reopen(RequestActor.current());
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional
    public OpportunityResponse decideDiscount(Long id, Long approverId, boolean approved, String comment) {
        Opportunity opportunity = load(id);
        SalesRepRef approver = salesRepRefRepository.findById(approverId)
                .orElseThrow(() -> new BusinessRuleException("aprovador %d nao encontrado na equipe comercial".formatted(approverId)));
        SalesRepRef owner = salesRepRefRepository.findById(opportunity.getOwnerId()).orElse(null);
        opportunity.decideDiscount(approver, owner, approved, comment);
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional
    public OpportunityResponse archive(Long id) {
        Opportunity opportunity = load(id);
        opportunity.archive();
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional
    public OpportunityResponse restore(Long id) {
        Opportunity opportunity = load(id);
        opportunity.restore();
        return toDetail(opportunityRepository.save(opportunity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRevision<OpportunityResponse>> findRevisions(Long id) {
        load(id);
        return opportunityRepository.findRevisions(id).stream()
                .map(revision -> revision.map(o -> OpportunityResponse.from(o, OpportunityResponse.Names.none(), false)))
                .toList();
    }

    private Opportunity load(Long id) {
        return opportunityRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Oportunidade", id));
    }

    private CompanyRef loadCompany(Long companyId) {
        return referenceRepository.findCompany(companyId)
                .orElseThrow(() -> new BusinessRuleException(
                        "empresa %d nao encontrada na base sincronizada de contas".formatted(companyId)));
    }

    private ContactRef loadContact(Long contactId) {
        return referenceRepository.findContact(contactId)
                .orElseThrow(() -> new BusinessRuleException(
                        "contato %d nao encontrado na base sincronizada de contas".formatted(contactId)));
    }

    private ProductRef loadProduct(Long productId) {
        return referenceRepository.findProduct(productId)
                .orElseThrow(() -> new BusinessRuleException(
                        "produto %d nao encontrado na base sincronizada do catalogo".formatted(productId)));
    }

    private SalesRepRef loadOwner(Long ownerId) {
        if (ownerId == null) {
            throw new BusinessRuleException("o lead precisa ter um vendedor responsavel para abrir a oportunidade");
        }
        return salesRepRefRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessRuleException(
                        "vendedor responsavel %d nao encontrado na base sincronizada da equipe comercial".formatted(ownerId)));
    }

    private static OpportunityDetails toDetails(OpportunityRequest request) {
        return new OpportunityDetails(request.title(), request.description(), request.expectedCloseDate(),
                request.termOrDefault(), request.estimatedValue());
    }

    private OpportunityResponse toDetail(Opportunity opportunity) {
        return OpportunityResponse.from(opportunity, namesFor(List.of(opportunity)).apply(opportunity), true);
    }

    private Function<Opportunity, OpportunityResponse.Names> namesFor(List<Opportunity> opportunities) {
        Set<Long> companyIds = opportunities.stream().map(Opportunity::getCompanyId).collect(Collectors.toSet());
        Set<Long> contactIds = opportunities.stream().map(Opportunity::getContactId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> ownerIds = opportunities.stream().map(Opportunity::getOwnerId).collect(Collectors.toSet());
        Map<Long, CompanyRef> companies = referenceRepository.findCompanies(companyIds);
        Map<Long, ContactRef> contacts = referenceRepository.findContacts(contactIds);
        Map<Long, SalesRepRef> owners = salesRepRefRepository.findAllByIds(ownerIds);
        return opportunity -> new OpportunityResponse.Names(
                companies.containsKey(opportunity.getCompanyId()) ? companies.get(opportunity.getCompanyId()).displayName() : null,
                opportunity.getContactId() != null && contacts.containsKey(opportunity.getContactId())
                        ? contacts.get(opportunity.getContactId()).fullName() : null,
                owners.containsKey(opportunity.getOwnerId()) ? owners.get(opportunity.getOwnerId()).name() : null);
    }

    private static BigDecimal sum(List<Opportunity> opportunities, Function<Opportunity, BigDecimal> value) {
        return opportunities.stream().map(value).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
