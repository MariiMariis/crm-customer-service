package com.pb.crm.accounts.application.company.dto;

import com.pb.crm.accounts.domain.company.BrazilianState;
import com.pb.crm.accounts.domain.company.Company;
import com.pb.crm.accounts.domain.company.CompanyProfile;
import com.pb.crm.accounts.domain.company.CompanySize;
import com.pb.crm.accounts.domain.company.CompanyType;
import com.pb.crm.accounts.domain.company.Industry;
import com.pb.crm.accounts.domain.contact.Contact;
import com.pb.crm.accounts.domain.salesrep.SalesRepRef;

import java.math.BigDecimal;
import java.time.Instant;

public record CompanyResponse(
        Long id,
        String legalName,
        String tradeName,
        String displayName,
        String cnpj,
        Industry industry,
        CompanySize size,
        Integer employees,
        BigDecimal annualRevenue,
        String website,
        String phone,
        String city,
        BrazilianState state,
        CompanyType type,
        Long ownerId,
        String ownerName,
        String notes,
        Long contactCount,
        Long primaryContactId,
        String primaryContactName,
        boolean archived,
        Instant archivedAt,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        Long version
) {
    public static CompanyResponse summary(Company company, SalesRepRef owner) {
        return build(company, owner, null, null);
    }

    public static CompanyResponse detail(Company company, SalesRepRef owner, long contactCount, Contact primaryContact) {
        return build(company, owner, contactCount, primaryContact);
    }

    private static CompanyResponse build(Company company, SalesRepRef owner, Long contactCount, Contact primaryContact) {
        CompanyProfile profile = company.getProfile();
        return new CompanyResponse(
                company.getId(),
                profile.legalName(),
                profile.tradeName(),
                company.displayName(),
                profile.cnpj().formatted(),
                profile.industry(),
                profile.size(),
                profile.employees(),
                profile.annualRevenue(),
                profile.website(),
                profile.phone(),
                profile.city(),
                profile.state(),
                profile.type(),
                company.getOwnerId(),
                owner == null ? null : owner.name(),
                profile.notes(),
                contactCount,
                primaryContact == null ? null : primaryContact.getId(),
                primaryContact == null ? null : primaryContact.fullName(),
                company.isArchived(),
                company.getArchivedAt(),
                company.getAudit().createdAt(),
                company.getAudit().updatedAt(),
                company.getAudit().createdBy(),
                company.getAudit().updatedBy(),
                company.getVersion()
        );
    }
}
