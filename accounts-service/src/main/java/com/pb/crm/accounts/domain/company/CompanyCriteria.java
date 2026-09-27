package com.pb.crm.accounts.domain.company;

public record CompanyCriteria(
        String term,
        Industry industry,
        CompanySize size,
        CompanyType type,
        BrazilianState state,
        Long ownerId,
        boolean includeArchived
) {
}
