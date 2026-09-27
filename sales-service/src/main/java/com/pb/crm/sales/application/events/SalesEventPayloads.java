package com.pb.crm.sales.application.events;

import com.pb.crm.sales.domain.activity.Activity;
import com.pb.crm.sales.domain.activity.ActivityStatus;
import com.pb.crm.sales.domain.activity.ActivityType;
import com.pb.crm.sales.domain.activity.RelatedType;
import com.pb.crm.sales.domain.lead.Lead;
import com.pb.crm.sales.domain.lead.LeadSource;
import com.pb.crm.sales.domain.lead.LeadStatus;
import com.pb.crm.sales.domain.opportunity.DiscountApprovalStatus;
import com.pb.crm.sales.domain.opportunity.Opportunity;
import com.pb.crm.sales.domain.opportunity.OpportunityStage;
import com.pb.crm.sales.domain.opportunity.StageChange;
import com.pb.crm.sales.domain.reference.SalesRepRef;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class SalesEventPayloads {

    private SalesEventPayloads() {
    }

    public record LeadPayload(
            Long id,
            String fullName,
            String email,
            String companyName,
            LeadSource source,
            LeadStatus status,
            int score,
            BigDecimal estimatedValue,
            Long ownerId,
            String ownerName,
            Long ownerManagerId,
            String disqualifyReason,
            Long convertedCompanyId,
            Long convertedContactId,
            Long convertedOpportunityId
    ) {
        public static LeadPayload from(Lead lead, SalesRepRef owner) {
            return new LeadPayload(
                    lead.getId(),
                    lead.fullName(),
                    lead.getDetails().email(),
                    lead.getDetails().companyName(),
                    lead.getDetails().source(),
                    lead.getStatus(),
                    lead.getScore(),
                    lead.getDetails().estimatedValue(),
                    lead.getOwnerId(),
                    owner == null ? null : owner.name(),
                    owner == null ? null : owner.managerId(),
                    lead.getDisqualifyReason(),
                    lead.getConvertedCompanyId(),
                    lead.getConvertedContactId(),
                    lead.getConvertedOpportunityId()
            );
        }
    }

    public record OpportunityPayload(
            Long id,
            String title,
            Long companyId,
            String companyName,
            Long ownerId,
            String ownerName,
            Long ownerManagerId,
            OpportunityStage stage,
            OpportunityStage previousStage,
            int probability,
            BigDecimal amount,
            BigDecimal weightedAmount,
            BigDecimal monthlyRecurringValue,
            LocalDate expectedCloseDate,
            String lossReason,
            DiscountApprovalStatus discountApproval,
            Long approvalDecidedBy,
            String approvalComment,
            Long leadId
    ) {
        public static OpportunityPayload from(Opportunity opportunity, String companyName, SalesRepRef owner) {
            List<StageChange> history = opportunity.getStageHistory();
            OpportunityStage previous = history.isEmpty() ? null : history.get(history.size() - 1).fromStage();
            return new OpportunityPayload(
                    opportunity.getId(),
                    opportunity.getDetails().title(),
                    opportunity.getCompanyId(),
                    companyName,
                    opportunity.getOwnerId(),
                    owner == null ? null : owner.name(),
                    owner == null ? null : owner.managerId(),
                    opportunity.getStage(),
                    previous,
                    opportunity.getProbability(),
                    opportunity.amount(),
                    opportunity.weightedAmount(),
                    opportunity.monthlyRecurringValue(),
                    opportunity.getDetails().expectedCloseDate(),
                    opportunity.getLossReason(),
                    opportunity.getDiscountApproval(),
                    opportunity.getApprovalDecidedBy(),
                    opportunity.getApprovalComment(),
                    opportunity.getLeadId()
            );
        }
    }

    public record ActivityPayload(
            Long id,
            ActivityType type,
            String subject,
            RelatedType relatedType,
            Long relatedId,
            String relatedName,
            Long ownerId,
            String ownerName,
            Instant dueAt,
            Instant startsAt,
            Instant endsAt,
            String location,
            ActivityStatus status,
            String outcome
    ) {
        public static ActivityPayload from(Activity activity, String relatedName, SalesRepRef owner) {
            return new ActivityPayload(
                    activity.getId(),
                    activity.getType(),
                    activity.getSubject(),
                    activity.getRelatedTo().type(),
                    activity.getRelatedTo().id(),
                    relatedName,
                    activity.getOwnerId(),
                    owner == null ? null : owner.name(),
                    activity.getSchedule().dueAt(),
                    activity.getSchedule().startsAt(),
                    activity.getSchedule().endsAt(),
                    activity.getSchedule().location(),
                    activity.getStatus(),
                    activity.getOutcome()
            );
        }
    }
}
