package com.pb.crm.sales.application.opportunity.dto;

import com.pb.crm.sales.domain.opportunity.BillingType;
import com.pb.crm.sales.domain.opportunity.DiscountApprovalStatus;
import com.pb.crm.sales.domain.opportunity.Opportunity;
import com.pb.crm.sales.domain.opportunity.OpportunityItem;
import com.pb.crm.sales.domain.opportunity.OpportunityStage;
import com.pb.crm.sales.domain.opportunity.StageChange;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record OpportunityResponse(
        Long id,
        String title,
        String description,
        Long companyId,
        String companyName,
        Long contactId,
        String contactName,
        Long ownerId,
        String ownerName,
        Long leadId,
        OpportunityStage stage,
        boolean open,
        int probability,
        LocalDate expectedCloseDate,
        int contractTermMonths,
        BigDecimal estimatedValue,
        BigDecimal oneTimeValue,
        BigDecimal monthlyRecurringValue,
        BigDecimal amount,
        BigDecimal weightedAmount,
        String lossReason,
        Instant closedAt,
        DiscountApprovalStatus discountApproval,
        Long approvalDecidedBy,
        String approvalComment,
        Instant approvalDecidedAt,
        List<Item> items,
        List<StageChange> stageHistory,
        boolean archived,
        Instant archivedAt,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        Long version
) {
    public record Item(
            Long id,
            Long productId,
            String sku,
            String productName,
            String category,
            BillingType billing,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal discountPercent,
            BigDecimal maxDiscountPercent,
            boolean aboveDiscountLimit,
            BigDecimal netTotal,
            BigDecimal monthlyValue
    ) {
        static Item from(OpportunityItem item) {
            return new Item(item.getId(), item.getProductId(), item.getSku(), item.getProductName(), item.getCategory(),
                    item.getBilling(), item.getQuantity(), item.getUnitPrice(), item.getDiscountPercent(),
                    item.getMaxDiscountPercent(), item.exceedsDiscountLimit(), item.netTotal(), item.monthlyValue());
        }
    }

    public record Names(String company, String contact, String owner) {

        public static Names none() {
            return new Names(null, null, null);
        }
    }

    public static OpportunityResponse from(Opportunity opportunity, Names names, boolean withDetails) {
        return new OpportunityResponse(
                opportunity.getId(),
                opportunity.getDetails().title(),
                opportunity.getDetails().description(),
                opportunity.getCompanyId(),
                names.company(),
                opportunity.getContactId(),
                names.contact(),
                opportunity.getOwnerId(),
                names.owner(),
                opportunity.getLeadId(),
                opportunity.getStage(),
                opportunity.getStage().isOpen(),
                opportunity.getProbability(),
                opportunity.getDetails().expectedCloseDate(),
                opportunity.getDetails().contractTermMonths(),
                opportunity.getDetails().estimatedValue(),
                opportunity.oneTimeValue(),
                opportunity.monthlyRecurringValue(),
                opportunity.amount(),
                opportunity.weightedAmount(),
                opportunity.getLossReason(),
                opportunity.getClosedAt(),
                opportunity.getDiscountApproval(),
                opportunity.getApprovalDecidedBy(),
                opportunity.getApprovalComment(),
                opportunity.getApprovalDecidedAt(),
                opportunity.getItems().stream().map(Item::from).toList(),
                withDetails ? opportunity.getStageHistory() : null,
                opportunity.isArchived(),
                opportunity.getArchivedAt(),
                opportunity.getAudit().createdAt(),
                opportunity.getAudit().updatedAt(),
                opportunity.getAudit().createdBy(),
                opportunity.getAudit().updatedBy(),
                opportunity.getVersion()
        );
    }
}
