package com.pb.crm.accounts.application.conversion;

public record LeadAccountRequest(
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
