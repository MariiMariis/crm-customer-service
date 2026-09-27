package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.commons.audit.ArchivableEntity;
import com.pb.crm.sales.domain.opportunity.DiscountApprovalStatus;
import com.pb.crm.sales.domain.opportunity.OpportunityStage;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
@Table(name = "opportunities")
public class OpportunityJpaEntity extends ArchivableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opportunities_seq")
    @SequenceGenerator(name = "opportunities_seq", sequenceName = "opportunities_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "lead_id")
    private Long leadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OpportunityStage stage;

    @Column(nullable = false)
    private int probability;

    @Column(name = "expected_close_date", nullable = false)
    private LocalDate expectedCloseDate;

    @Column(name = "contract_term_months", nullable = false)
    private int contractTermMonths;

    @Column(name = "estimated_value", precision = 17, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "one_time_value", nullable = false, precision = 17, scale = 2)
    private BigDecimal oneTimeValue;

    @Column(name = "monthly_recurring_value", nullable = false, precision = 17, scale = 2)
    private BigDecimal monthlyRecurringValue;

    @Column(nullable = false, precision = 17, scale = 2)
    private BigDecimal amount;

    @Column(name = "weighted_amount", nullable = false, precision = 17, scale = 2)
    private BigDecimal weightedAmount;

    @Column(name = "loss_reason", length = 500)
    private String lossReason;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_approval", nullable = false, length = 20)
    private DiscountApprovalStatus discountApproval;

    @Column(name = "approval_decided_by")
    private Long approvalDecidedBy;

    @Column(name = "approval_comment", length = 500)
    private String approvalComment;

    @Column(name = "approval_decided_at")
    private Instant approvalDecidedAt;

    @NotAudited
    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<OpportunityItemJpaEntity> items = new ArrayList<>();

    @NotAudited
    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("changedAt ASC, id ASC")
    private List<OpportunityStageChangeJpaEntity> stageHistory = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public OpportunityStage getStage() {
        return stage;
    }

    public void setStage(OpportunityStage stage) {
        this.stage = stage;
    }

    public int getProbability() {
        return probability;
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }

    public LocalDate getExpectedCloseDate() {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(LocalDate expectedCloseDate) {
        this.expectedCloseDate = expectedCloseDate;
    }

    public int getContractTermMonths() {
        return contractTermMonths;
    }

    public void setContractTermMonths(int contractTermMonths) {
        this.contractTermMonths = contractTermMonths;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public BigDecimal getOneTimeValue() {
        return oneTimeValue;
    }

    public void setOneTimeValue(BigDecimal oneTimeValue) {
        this.oneTimeValue = oneTimeValue;
    }

    public BigDecimal getMonthlyRecurringValue() {
        return monthlyRecurringValue;
    }

    public void setMonthlyRecurringValue(BigDecimal monthlyRecurringValue) {
        this.monthlyRecurringValue = monthlyRecurringValue;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getWeightedAmount() {
        return weightedAmount;
    }

    public void setWeightedAmount(BigDecimal weightedAmount) {
        this.weightedAmount = weightedAmount;
    }

    public String getLossReason() {
        return lossReason;
    }

    public void setLossReason(String lossReason) {
        this.lossReason = lossReason;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public DiscountApprovalStatus getDiscountApproval() {
        return discountApproval;
    }

    public void setDiscountApproval(DiscountApprovalStatus discountApproval) {
        this.discountApproval = discountApproval;
    }

    public Long getApprovalDecidedBy() {
        return approvalDecidedBy;
    }

    public void setApprovalDecidedBy(Long approvalDecidedBy) {
        this.approvalDecidedBy = approvalDecidedBy;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    public Instant getApprovalDecidedAt() {
        return approvalDecidedAt;
    }

    public void setApprovalDecidedAt(Instant approvalDecidedAt) {
        this.approvalDecidedAt = approvalDecidedAt;
    }

    public List<OpportunityItemJpaEntity> getItems() {
        return items;
    }

    public List<OpportunityStageChangeJpaEntity> getStageHistory() {
        return stageHistory;
    }
}
