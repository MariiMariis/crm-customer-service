package com.pb.crm.accounts.application.events;

import com.pb.crm.accounts.domain.company.BrazilianState;
import com.pb.crm.accounts.domain.company.Company;
import com.pb.crm.accounts.domain.company.CompanyProfile;
import com.pb.crm.accounts.domain.company.CompanySize;
import com.pb.crm.accounts.domain.company.CompanyType;
import com.pb.crm.accounts.domain.company.Industry;

public record CompanySnapshot(
        Long id,
        String legalName,
        String tradeName,
        String displayName,
        String cnpj,
        Industry industry,
        CompanySize size,
        String city,
        BrazilianState state,
        CompanyType type,
        Long ownerId,
        boolean archived,
        Long version
) {
    public static CompanySnapshot from(Company company) {
        CompanyProfile profile = company.getProfile();
        return new CompanySnapshot(
                company.getId(),
                profile.legalName(),
                profile.tradeName(),
                company.displayName(),
                profile.cnpj().value(),
                profile.industry(),
                profile.size(),
                profile.city(),
                profile.state(),
                profile.type(),
                company.getOwnerId(),
                company.isArchived(),
                company.getVersion()
        );
    }
}
