package com.pb.crm.sales.application.lead.events;

import com.pb.crm.sales.domain.lead.ConversionRequest;
import com.pb.crm.sales.domain.lead.Lead;
import com.pb.crm.sales.domain.lead.LeadDetails;

import java.time.LocalDate;

public record LeadConversionRequested(
        Long leadId,
        String companyName,
        String cnpj,
        String industry,
        String companySize,
        String city,
        String state,
        String firstName,
        String lastName,
        String email,
        String phone,
        String jobTitle,
        Long ownerId,
        boolean createOpportunity,
        String opportunityTitle,
        LocalDate expectedCloseDate
) {
    public static LeadConversionRequested from(Lead lead) {
        LeadDetails details = lead.getDetails();
        ConversionRequest conversion = lead.getConversion();
        return new LeadConversionRequested(
                lead.getId(),
                details.companyName(),
                conversion.cnpj(),
                conversion.industry(),
                conversion.companySize(),
                conversion.city(),
                conversion.state(),
                details.firstName(),
                details.lastName(),
                details.email(),
                details.phone(),
                details.jobTitle(),
                lead.getOwnerId(),
                conversion.createOpportunity(),
                conversion.opportunityTitle(),
                conversion.expectedCloseDate()
        );
    }
}
