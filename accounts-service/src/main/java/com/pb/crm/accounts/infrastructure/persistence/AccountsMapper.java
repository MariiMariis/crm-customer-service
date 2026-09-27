package com.pb.crm.accounts.infrastructure.persistence;

import com.pb.crm.accounts.domain.company.Cnpj;
import com.pb.crm.accounts.domain.company.Company;
import com.pb.crm.accounts.domain.company.CompanyProfile;
import com.pb.crm.accounts.domain.contact.Contact;
import com.pb.crm.accounts.domain.contact.ContactProfile;
import com.pb.crm.accounts.domain.salesrep.SalesRepRef;
import org.springframework.stereotype.Component;

@Component
public class AccountsMapper {

    public Company toDomain(CompanyJpaEntity entity) {
        CompanyProfile profile = new CompanyProfile(
                entity.getLegalName(),
                entity.getTradeName(),
                new Cnpj(entity.getCnpj()),
                entity.getIndustry(),
                entity.getSize(),
                entity.getEmployees(),
                entity.getAnnualRevenue(),
                entity.getWebsite(),
                entity.getPhone(),
                entity.getCity(),
                entity.getState(),
                entity.getType(),
                entity.getNotes()
        );
        return Company.rehydrate(
                entity.getId(),
                profile,
                entity.getOwnerId(),
                entity.isArchived(),
                entity.getArchivedAt(),
                entity.toAuditInfo()
        );
    }

    public void copyToEntity(Company company, CompanyJpaEntity entity) {
        CompanyProfile profile = company.getProfile();
        entity.setLegalName(profile.legalName());
        entity.setTradeName(profile.tradeName());
        entity.setCnpj(profile.cnpj().value());
        entity.setIndustry(profile.industry());
        entity.setSize(profile.size());
        entity.setEmployees(profile.employees());
        entity.setAnnualRevenue(profile.annualRevenue());
        entity.setWebsite(profile.website());
        entity.setPhone(profile.phone());
        entity.setCity(profile.city());
        entity.setState(profile.state());
        entity.setType(profile.type());
        entity.setNotes(profile.notes());
        entity.setOwnerId(company.getOwnerId());
        entity.applyArchiveState(company.isArchived(), company.getArchivedAt());
    }

    public Contact toDomain(ContactJpaEntity entity) {
        ContactProfile profile = new ContactProfile(
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getMobile(),
                entity.getJobTitle(),
                entity.getDepartment(),
                entity.getDecisionRole()
        );
        return Contact.rehydrate(
                entity.getId(),
                entity.getCompanyId(),
                profile,
                entity.isPrimary(),
                entity.isActive(),
                entity.isArchived(),
                entity.getArchivedAt(),
                entity.toAuditInfo()
        );
    }

    public void copyToEntity(Contact contact, ContactJpaEntity entity) {
        ContactProfile profile = contact.getProfile();
        entity.setCompanyId(contact.getCompanyId());
        entity.setFirstName(profile.firstName());
        entity.setLastName(profile.lastName());
        entity.setEmail(profile.email());
        entity.setPhone(profile.phone());
        entity.setMobile(profile.mobile());
        entity.setJobTitle(profile.jobTitle());
        entity.setDepartment(profile.department());
        entity.setDecisionRole(profile.decisionRole());
        entity.setPrimary(contact.isPrimary());
        entity.setActive(contact.isActive());
        entity.applyArchiveState(contact.isArchived(), contact.getArchivedAt());
    }

    public SalesRepRef toDomain(SalesRepRefJpaEntity entity) {
        return new SalesRepRef(entity.getId(), entity.getName(), entity.getEmail(), entity.isActive(), entity.isArchived());
    }
}
