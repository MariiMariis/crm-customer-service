package com.pb.crm.sales.application.lead.dto;

import com.pb.crm.sales.domain.lead.ConversionRequest;
import com.pb.crm.sales.domain.lead.Lead;
import com.pb.crm.sales.domain.lead.LeadDetails;
import com.pb.crm.sales.domain.lead.LeadSource;
import com.pb.crm.sales.domain.lead.LeadStatus;
import com.pb.crm.sales.domain.lead.ScoreFactor;
import com.pb.crm.sales.domain.reference.SalesRepRef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record LeadResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String companyName,
        String jobTitle,
        LeadSource source,
        LeadStatus status,
        int score,
        List<ScoreFactor> scoreFactors,
        BigDecimal estimatedValue,
        Long ownerId,
        String ownerName,
        String notes,
        String disqualifyReason,
        ConversionRequest conversion,
        String conversionFailureReason,
        Long convertedCompanyId,
        Long convertedContactId,
        Long convertedOpportunityId,
        Instant convertedAt,
        boolean archived,
        Instant archivedAt,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        Long version
) {
    public static LeadResponse from(Lead lead, SalesRepRef owner, boolean withScoreFactors) {
        LeadDetails details = lead.getDetails();
        return new LeadResponse(
                lead.getId(),
                details.firstName(),
                details.lastName(),
                lead.fullName(),
                details.email(),
                details.phone(),
                details.companyName(),
                details.jobTitle(),
                details.source(),
                lead.getStatus(),
                lead.getScore(),
                withScoreFactors ? lead.scoreBreakdown().factors() : null,
                details.estimatedValue(),
                lead.getOwnerId(),
                owner == null ? null : owner.name(),
                details.notes(),
                lead.getDisqualifyReason(),
                lead.getConversion(),
                lead.getConversionFailureReason(),
                lead.getConvertedCompanyId(),
                lead.getConvertedContactId(),
                lead.getConvertedOpportunityId(),
                lead.getConvertedAt(),
                lead.isArchived(),
                lead.getArchivedAt(),
                lead.getAudit().createdAt(),
                lead.getAudit().updatedAt(),
                lead.getAudit().createdBy(),
                lead.getAudit().updatedBy(),
                lead.getVersion()
        );
    }
}
