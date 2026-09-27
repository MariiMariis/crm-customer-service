package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.opportunity.Opportunity;
import com.pb.crm.sales.domain.opportunity.OpportunityDetails;
import com.pb.crm.sales.domain.opportunity.OpportunityItem;
import com.pb.crm.sales.domain.opportunity.StageChange;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OpportunityMapper {

    public Opportunity toDomain(OpportunityJpaEntity entity) {
        OpportunityDetails details = new OpportunityDetails(
                entity.getTitle(),
                entity.getDescription(),
                entity.getExpectedCloseDate(),
                entity.getContractTermMonths(),
                entity.getEstimatedValue()
        );
        List<OpportunityItem> items = entity.getItems().stream()
                .map(item -> OpportunityItem.rehydrate(
                        item.getId(),
                        item.getProductId(),
                        item.getSku(),
                        item.getProductName(),
                        item.getCategory(),
                        item.getBilling(),
                        item.getUnitPrice(),
                        item.getMaxDiscountPercent(),
                        item.getQuantity(),
                        item.getDiscountPercent()))
                .toList();
        List<StageChange> history = entity.getStageHistory().stream()
                .map(change -> new StageChange(
                        change.getId(),
                        change.getFromStage(),
                        change.getToStage(),
                        change.getProbability(),
                        change.getReason(),
                        change.getChangedBy(),
                        change.getChangedAt()))
                .toList();
        return Opportunity.rehydrate(
                entity.getId(),
                details,
                entity.getCompanyId(),
                entity.getContactId(),
                entity.getOwnerId(),
                entity.getLeadId(),
                entity.getStage(),
                entity.getProbability(),
                entity.getLossReason(),
                entity.getClosedAt(),
                entity.getDiscountApproval(),
                entity.getApprovalDecidedBy(),
                entity.getApprovalComment(),
                entity.getApprovalDecidedAt(),
                items,
                history,
                entity.isArchived(),
                entity.getArchivedAt(),
                entity.toAuditInfo()
        );
    }

    public Opportunity toRevisionSnapshot(OpportunityJpaEntity entity) {
        OpportunityDetails details = new OpportunityDetails(
                entity.getTitle(),
                entity.getDescription(),
                entity.getExpectedCloseDate(),
                entity.getContractTermMonths(),
                entity.getEstimatedValue()
        );
        return Opportunity.rehydrate(entity.getId(), details, entity.getCompanyId(), entity.getContactId(),
                entity.getOwnerId(), entity.getLeadId(), entity.getStage(), entity.getProbability(),
                entity.getLossReason(), entity.getClosedAt(), entity.getDiscountApproval(), entity.getApprovalDecidedBy(),
                entity.getApprovalComment(), entity.getApprovalDecidedAt(), List.of(), List.of(), entity.isArchived(),
                entity.getArchivedAt(), entity.toAuditInfo());
    }

    public void copyToEntity(Opportunity opportunity, OpportunityJpaEntity entity) {
        OpportunityDetails details = opportunity.getDetails();
        entity.setTitle(details.title());
        entity.setDescription(details.description());
        entity.setExpectedCloseDate(details.expectedCloseDate());
        entity.setContractTermMonths(details.contractTermMonths());
        entity.setEstimatedValue(details.estimatedValue());
        entity.setCompanyId(opportunity.getCompanyId());
        entity.setContactId(opportunity.getContactId());
        entity.setOwnerId(opportunity.getOwnerId());
        entity.setLeadId(opportunity.getLeadId());
        entity.setStage(opportunity.getStage());
        entity.setProbability(opportunity.getProbability());
        entity.setOneTimeValue(opportunity.oneTimeValue());
        entity.setMonthlyRecurringValue(opportunity.monthlyRecurringValue());
        entity.setAmount(opportunity.amount());
        entity.setWeightedAmount(opportunity.weightedAmount());
        entity.setLossReason(opportunity.getLossReason());
        entity.setClosedAt(opportunity.getClosedAt());
        entity.setDiscountApproval(opportunity.getDiscountApproval());
        entity.setApprovalDecidedBy(opportunity.getApprovalDecidedBy());
        entity.setApprovalComment(opportunity.getApprovalComment());
        entity.setApprovalDecidedAt(opportunity.getApprovalDecidedAt());
        entity.applyArchiveState(opportunity.isArchived(), opportunity.getArchivedAt());
        syncItems(opportunity, entity);
        appendStageChanges(opportunity, entity);
    }

    private void syncItems(Opportunity opportunity, OpportunityJpaEntity entity) {
        Map<Long, OpportunityItemJpaEntity> existing = entity.getItems().stream()
                .collect(Collectors.toMap(OpportunityItemJpaEntity::getId, Function.identity()));
        List<Long> keptIds = opportunity.getItems().stream()
                .map(OpportunityItem::getId)
                .filter(Objects::nonNull)
                .toList();
        entity.getItems().removeIf(item -> !keptIds.contains(item.getId()));
        for (OpportunityItem item : opportunity.getItems()) {
            OpportunityItemJpaEntity target = item.getId() == null ? null : existing.get(item.getId());
            if (target == null) {
                target = new OpportunityItemJpaEntity(entity);
                entity.getItems().add(target);
            }
            target.apply(item.getProductId(), item.getSku(), item.getProductName(), item.getCategory(), item.getBilling(),
                    item.getUnitPrice(), item.getMaxDiscountPercent(), item.getQuantity(), item.getDiscountPercent(),
                    item.netTotal());
        }
    }

    private void appendStageChanges(Opportunity opportunity, OpportunityJpaEntity entity) {
        opportunity.getStageHistory().stream()
                .filter(change -> change.id() == null)
                .forEach(change -> entity.getStageHistory().add(new OpportunityStageChangeJpaEntity(
                        entity,
                        change.fromStage(),
                        change.toStage(),
                        change.probability(),
                        change.reason(),
                        change.changedBy(),
                        change.changedAt())));
    }
}
