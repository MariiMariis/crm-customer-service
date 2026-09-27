package com.pb.crm.accounts.infrastructure.messaging;

public record LeadConversionRequestedPayload(
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
        Long ownerId
) {
}
