package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.lead.ConversionRequest;
import com.pb.crm.sales.domain.lead.Lead;
import com.pb.crm.sales.domain.lead.LeadDetails;
import org.springframework.stereotype.Component;

@Component
public class LeadMapper {

    public Lead toDomain(LeadJpaEntity entity) {
        LeadDetails details = new LeadDetails(
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getCompanyName(),
                entity.getJobTitle(),
                entity.getSource(),
                entity.getEstimatedValue(),
                entity.getNotes()
        );
        ConversionRequest conversion = entity.getConversionCnpj() == null
                ? null
                : new ConversionRequest(
                        entity.getConversionCnpj(),
                        entity.getConversionIndustry(),
                        entity.getConversionCompanySize(),
                        entity.getConversionCity(),
                        entity.getConversionState(),
                        Boolean.TRUE.equals(entity.getConversionCreateOpportunity()),
                        entity.getConversionOpportunityTitle(),
                        entity.getConversionExpectedCloseDate(),
                        entity.getConversionRequestedAt());
        return Lead.rehydrate(
                entity.getId(),
                details,
                entity.getStatus(),
                entity.getScore(),
                entity.getOwnerId(),
                entity.getDisqualifyReason(),
                conversion,
                entity.getConversionFailureReason(),
                entity.getConvertedCompanyId(),
                entity.getConvertedContactId(),
                entity.getConvertedOpportunityId(),
                entity.getConvertedAt(),
                entity.isArchived(),
                entity.getArchivedAt(),
                entity.toAuditInfo()
        );
    }

    public void copyToEntity(Lead lead, LeadJpaEntity entity) {
        LeadDetails details = lead.getDetails();
        entity.setFirstName(details.firstName());
        entity.setLastName(details.lastName());
        entity.setEmail(details.email());
        entity.setPhone(details.phone());
        entity.setCompanyName(details.companyName());
        entity.setJobTitle(details.jobTitle());
        entity.setSource(details.source());
        entity.setEstimatedValue(details.estimatedValue());
        entity.setNotes(details.notes());
        entity.setStatus(lead.getStatus());
        entity.setScore(lead.getScore());
        entity.setOwnerId(lead.getOwnerId());
        entity.setDisqualifyReason(lead.getDisqualifyReason());
        ConversionRequest conversion = lead.getConversion();
        entity.setConversionCnpj(conversion == null ? null : conversion.cnpj());
        entity.setConversionIndustry(conversion == null ? null : conversion.industry());
        entity.setConversionCompanySize(conversion == null ? null : conversion.companySize());
        entity.setConversionCity(conversion == null ? null : conversion.city());
        entity.setConversionState(conversion == null ? null : conversion.state());
        entity.setConversionCreateOpportunity(conversion == null ? null : conversion.createOpportunity());
        entity.setConversionOpportunityTitle(conversion == null ? null : conversion.opportunityTitle());
        entity.setConversionExpectedCloseDate(conversion == null ? null : conversion.expectedCloseDate());
        entity.setConversionRequestedAt(conversion == null ? null : conversion.requestedAt());
        entity.setConversionFailureReason(lead.getConversionFailureReason());
        entity.setConvertedCompanyId(lead.getConvertedCompanyId());
        entity.setConvertedContactId(lead.getConvertedContactId());
        entity.setConvertedOpportunityId(lead.getConvertedOpportunityId());
        entity.setConvertedAt(lead.getConvertedAt());
        entity.applyArchiveState(lead.isArchived(), lead.getArchivedAt());
    }
}
