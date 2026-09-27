package com.pb.crm.sales.domain.opportunity;

import com.pb.crm.commons.domain.AggregateRoot;
import com.pb.crm.commons.domain.AuditInfo;
import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.sales.domain.reference.CompanyRef;
import com.pb.crm.sales.domain.reference.ContactRef;
import com.pb.crm.sales.domain.reference.ProductRef;
import com.pb.crm.sales.domain.reference.SalesRepRef;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Opportunity extends AggregateRoot {

    public static final int MIN_TERM_MONTHS = 1;
    public static final int MAX_TERM_MONTHS = 60;

    private OpportunityDetails details;
    private Long companyId;
    private Long contactId;
    private Long ownerId;
    private Long leadId;
    private OpportunityStage stage;
    private int probability;
    private String lossReason;
    private Instant closedAt;
    private DiscountApprovalStatus discountApproval;
    private Long approvalDecidedBy;
    private String approvalComment;
    private Instant approvalDecidedAt;
    private List<OpportunityItem> items = new ArrayList<>();
    private List<StageChange> stageHistory = new ArrayList<>();

    private Opportunity() {
    }

    public static Opportunity open(OpportunityDetails details,
                                   CompanyRef company,
                                   ContactRef contact,
                                   SalesRepRef owner,
                                   Long leadId,
                                   String actor) {
        Opportunity opportunity = new Opportunity();
        opportunity.details = validate(details);
        opportunity.attachCompany(company, contact);
        opportunity.assignOwner(owner);
        opportunity.leadId = leadId;
        opportunity.stage = OpportunityStage.PROSPECTING;
        opportunity.probability = OpportunityStage.PROSPECTING.defaultProbability();
        opportunity.discountApproval = DiscountApprovalStatus.NOT_REQUIRED;
        opportunity.record(null, OpportunityStage.PROSPECTING, "abertura da oportunidade", actor);
        return opportunity;
    }

    public static Opportunity rehydrate(Long id,
                                        OpportunityDetails details,
                                        Long companyId,
                                        Long contactId,
                                        Long ownerId,
                                        Long leadId,
                                        OpportunityStage stage,
                                        int probability,
                                        String lossReason,
                                        Instant closedAt,
                                        DiscountApprovalStatus discountApproval,
                                        Long approvalDecidedBy,
                                        String approvalComment,
                                        Instant approvalDecidedAt,
                                        List<OpportunityItem> items,
                                        List<StageChange> stageHistory,
                                        boolean archived,
                                        Instant archivedAt,
                                        AuditInfo audit) {
        Opportunity opportunity = new Opportunity();
        opportunity.rehydrateBase(id, audit, archived, archivedAt);
        opportunity.details = details;
        opportunity.companyId = companyId;
        opportunity.contactId = contactId;
        opportunity.ownerId = ownerId;
        opportunity.leadId = leadId;
        opportunity.stage = stage;
        opportunity.probability = probability;
        opportunity.lossReason = lossReason;
        opportunity.closedAt = closedAt;
        opportunity.discountApproval = discountApproval;
        opportunity.approvalDecidedBy = approvalDecidedBy;
        opportunity.approvalComment = approvalComment;
        opportunity.approvalDecidedAt = approvalDecidedAt;
        opportunity.items = new ArrayList<>(items);
        opportunity.stageHistory = new ArrayList<>(stageHistory);
        return opportunity;
    }

    public void updateDetails(OpportunityDetails newDetails, ContactRef contact) {
        assertOpen("editar");
        this.details = validate(newDetails);
        if (contact == null) {
            this.contactId = null;
        } else if (!contact.id().equals(contactId)) {
            assertContactBelongsToCompany(contact);
            this.contactId = contact.id();
        }
    }

    public void reassign(SalesRepRef owner) {
        assertOpen("reatribuir");
        assignOwner(owner);
    }

    public OpportunityItem addItem(ProductRef product, int quantity, BigDecimal discountPercent) {
        assertOpen("alterar os itens de");
        OpportunityItem item = OpportunityItem.of(product, quantity, discountPercent);
        items.add(item);
        reevaluateDiscountApproval();
        return item;
    }

    public void changeItem(Long itemId, int quantity, BigDecimal discountPercent) {
        assertOpen("alterar os itens de");
        findItem(itemId).change(quantity, discountPercent);
        reevaluateDiscountApproval();
    }

    public void removeItem(Long itemId) {
        assertOpen("alterar os itens de");
        items.remove(findItem(itemId));
        reevaluateDiscountApproval();
    }

    public void moveTo(OpportunityStage target, String actor) {
        assertOpen("mover");
        if (target == null || !target.isOpen()) {
            throw new BusinessRuleException("use as acoes de ganho ou perda para encerrar a oportunidade");
        }
        if (target == stage) {
            throw new BusinessRuleException("a oportunidade ja esta na etapa " + target);
        }
        OpportunityStage previous = stage;
        stage = target;
        probability = target.defaultProbability();
        record(previous, target, null, actor);
    }

    public void adjustProbability(int newProbability) {
        assertOpen("ajustar a probabilidade de");
        if (newProbability < 1 || newProbability > 99) {
            throw new BusinessRuleException("a probabilidade de uma oportunidade aberta deve estar entre 1 e 99%");
        }
        this.probability = newProbability;
    }

    public void markWon(String actor) {
        assertOpen("ganhar");
        if (items.isEmpty()) {
            throw new BusinessRuleException("inclua ao menos um item antes de marcar a oportunidade como ganha");
        }
        if (discountApproval.blocksClosing()) {
            throw new BusinessRuleException("existe desconto acima do limite sem aprovacao do gestor (status %s)"
                    .formatted(discountApproval));
        }
        close(OpportunityStage.WON, null, actor);
    }

    public void markLost(String reason, String actor) {
        assertOpen("perder");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("o motivo da perda e obrigatorio");
        }
        close(OpportunityStage.LOST, reason.trim(), actor);
    }

    public void reopen(String actor) {
        assertNotArchived();
        if (stage.isOpen()) {
            throw new BusinessRuleException("a oportunidade ja esta aberta");
        }
        OpportunityStage previous = stage;
        OpportunityStage target = stageBeforeClosing();
        stage = target;
        probability = target.defaultProbability();
        lossReason = null;
        closedAt = null;
        record(previous, target, "reabertura", actor);
    }

    public void decideDiscount(SalesRepRef approver, SalesRepRef owner, boolean approved, String comment) {
        assertOpen("aprovar o desconto de");
        if (discountApproval != DiscountApprovalStatus.PENDING) {
            throw new BusinessRuleException("nao ha desconto aguardando aprovacao nesta oportunidade");
        }
        if (approver == null || owner == null || owner.managerId() == null || !owner.managerId().equals(approver.id())) {
            throw new BusinessRuleException("apenas o gestor do vendedor responsavel pode decidir sobre o desconto");
        }
        if (!approved && (comment == null || comment.isBlank())) {
            throw new IllegalArgumentException("informe o motivo da rejeicao do desconto");
        }
        this.discountApproval = approved ? DiscountApprovalStatus.APPROVED : DiscountApprovalStatus.REJECTED;
        this.approvalDecidedBy = approver.id();
        this.approvalComment = comment == null || comment.isBlank() ? null : comment.trim();
        this.approvalDecidedAt = Instant.now();
    }

    public BigDecimal oneTimeValue() {
        return items.stream().map(OpportunityItem::oneTimeValue).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    public BigDecimal monthlyRecurringValue() {
        return items.stream().map(OpportunityItem::monthlyValue).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    public BigDecimal amount() {
        if (items.isEmpty()) {
            return details.estimatedValue() == null ? BigDecimal.ZERO.setScale(2) : details.estimatedValue();
        }
        return oneTimeValue().add(monthlyRecurringValue().multiply(BigDecimal.valueOf(details.contractTermMonths())));
    }

    public BigDecimal weightedAmount() {
        return amount().multiply(BigDecimal.valueOf(probability))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public boolean hasItemAboveDiscountLimit() {
        return items.stream().anyMatch(OpportunityItem::exceedsDiscountLimit);
    }

    private void reevaluateDiscountApproval() {
        if (hasItemAboveDiscountLimit()) {
            discountApproval = DiscountApprovalStatus.PENDING;
        } else {
            discountApproval = DiscountApprovalStatus.NOT_REQUIRED;
        }
        approvalDecidedBy = null;
        approvalComment = null;
        approvalDecidedAt = null;
    }

    private void close(OpportunityStage target, String reason, String actor) {
        OpportunityStage previous = stage;
        stage = target;
        probability = target.defaultProbability();
        lossReason = reason;
        closedAt = Instant.now();
        record(previous, target, reason, actor);
    }

    private OpportunityStage stageBeforeClosing() {
        for (int i = stageHistory.size() - 1; i >= 0; i--) {
            StageChange change = stageHistory.get(i);
            if (change.toStage() == stage && change.fromStage() != null && change.fromStage().isOpen()) {
                return change.fromStage();
            }
        }
        return OpportunityStage.NEGOTIATION;
    }

    private void record(OpportunityStage from, OpportunityStage to, String reason, String actor) {
        stageHistory.add(new StageChange(null, from, to, probability, reason, actor, Instant.now()));
    }

    private OpportunityItem findItem(Long itemId) {
        return items.stream()
                .filter(item -> Objects.equals(item.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("item %s nao pertence a esta oportunidade".formatted(itemId)));
    }

    private void attachCompany(CompanyRef company, ContactRef contact) {
        if (company == null) {
            throw new IllegalArgumentException("empresa e obrigatoria");
        }
        if (!company.acceptsOpportunities()) {
            throw new BusinessRuleException("a empresa esta arquivada e nao pode receber oportunidades");
        }
        this.companyId = company.id();
        if (contact != null) {
            assertContactBelongsToCompany(contact);
            this.contactId = contact.id();
        }
    }

    private void assertContactBelongsToCompany(ContactRef contact) {
        if (!contact.companyId().equals(companyId)) {
            throw new BusinessRuleException("o contato informado nao pertence a empresa da oportunidade");
        }
        if (!contact.isAvailable()) {
            throw new BusinessRuleException("o contato informado esta inativo ou arquivado");
        }
    }

    private void assignOwner(SalesRepRef owner) {
        if (owner == null) {
            throw new IllegalArgumentException("vendedor responsavel e obrigatorio");
        }
        if (!owner.canOwnRecords()) {
            throw new BusinessRuleException("o vendedor informado esta inativo ou arquivado");
        }
        this.ownerId = owner.id();
    }

    private void assertOpen(String action) {
        assertNotArchived();
        if (!stage.isOpen()) {
            throw new BusinessRuleException("nao e possivel %s uma oportunidade encerrada (%s)".formatted(action, stage));
        }
    }

    private static OpportunityDetails validate(OpportunityDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("dados da oportunidade sao obrigatorios");
        }
        if (details.title() == null || details.title().isBlank()) {
            throw new IllegalArgumentException("titulo e obrigatorio");
        }
        if (details.expectedCloseDate() == null) {
            throw new IllegalArgumentException("data prevista de fechamento e obrigatoria");
        }
        if (details.contractTermMonths() < MIN_TERM_MONTHS || details.contractTermMonths() > MAX_TERM_MONTHS) {
            throw new BusinessRuleException("o prazo do contrato deve estar entre %d e %d meses"
                    .formatted(MIN_TERM_MONTHS, MAX_TERM_MONTHS));
        }
        if (details.estimatedValue() != null && details.estimatedValue().signum() < 0) {
            throw new BusinessRuleException("o valor estimado nao pode ser negativo");
        }
        String description = details.description() == null || details.description().isBlank()
                ? null
                : details.description().trim();
        return new OpportunityDetails(details.title().trim(), description, details.expectedCloseDate(),
                details.contractTermMonths(), details.estimatedValue());
    }

    public OpportunityDetails getDetails() {
        return details;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Long getContactId() {
        return contactId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public Long getLeadId() {
        return leadId;
    }

    public OpportunityStage getStage() {
        return stage;
    }

    public int getProbability() {
        return probability;
    }

    public String getLossReason() {
        return lossReason;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public DiscountApprovalStatus getDiscountApproval() {
        return discountApproval;
    }

    public Long getApprovalDecidedBy() {
        return approvalDecidedBy;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public Instant getApprovalDecidedAt() {
        return approvalDecidedAt;
    }

    public List<OpportunityItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<StageChange> getStageHistory() {
        return Collections.unmodifiableList(stageHistory);
    }
}
