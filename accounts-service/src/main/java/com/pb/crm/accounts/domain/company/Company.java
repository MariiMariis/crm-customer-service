package com.pb.crm.accounts.domain.company;

import com.pb.crm.accounts.domain.salesrep.SalesRepRef;
import com.pb.crm.commons.domain.AggregateRoot;
import com.pb.crm.commons.domain.AuditInfo;
import com.pb.crm.commons.error.BusinessRuleException;

import java.time.Instant;

public class Company extends AggregateRoot {

    public static final String CREATED = "company.created";
    public static final String UPDATED = "company.updated";
    public static final String ARCHIVED = "company.archived";
    public static final String RESTORED = "company.restored";

    private CompanyProfile profile;
    private Long ownerId;

    private Company() {
    }

    public static Company register(CompanyProfile profile, SalesRepRef owner) {
        Company company = new Company();
        company.profile = validate(profile);
        company.assignOwner(owner);
        company.recordEvent(CREATED);
        return company;
    }

    public static Company rehydrate(Long id,
                                    CompanyProfile profile,
                                    Long ownerId,
                                    boolean archived,
                                    Instant archivedAt,
                                    AuditInfo audit) {
        Company company = new Company();
        company.rehydrateBase(id, audit, archived, archivedAt);
        company.profile = profile;
        company.ownerId = ownerId;
        return company;
    }

    public void update(CompanyProfile profile) {
        assertNotArchived();
        this.profile = validate(profile);
        recordEvent(UPDATED);
    }

    public void reassignOwner(SalesRepRef owner) {
        assertNotArchived();
        assignOwner(owner);
        recordEvent(UPDATED);
    }

    public boolean promoteToCustomer() {
        if (isArchived()) {
            return false;
        }
        CompanyType current = profile.type();
        if (current != CompanyType.PROSPECT && current != CompanyType.FORMER_CUSTOMER) {
            return false;
        }
        this.profile = new CompanyProfile(profile.legalName(), profile.tradeName(), profile.cnpj(), profile.industry(),
                profile.size(), profile.employees(), profile.annualRevenue(), profile.website(), profile.phone(),
                profile.city(), profile.state(), CompanyType.CUSTOMER, profile.notes());
        recordEvent(UPDATED);
        return true;
    }

    @Override
    public void archive() {
        super.archive();
        recordEvent(ARCHIVED);
    }

    @Override
    public void restore() {
        super.restore();
        recordEvent(RESTORED);
    }

    public boolean acceptsNewContacts() {
        return !isArchived();
    }

    private void assignOwner(SalesRepRef owner) {
        if (owner == null) {
            this.ownerId = null;
            return;
        }
        if (!owner.canOwnRecords()) {
            throw new BusinessRuleException("o vendedor responsavel informado esta inativo ou arquivado");
        }
        this.ownerId = owner.id();
    }

    private static CompanyProfile validate(CompanyProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("dados da empresa sao obrigatorios");
        }
        String legalName = requireText(profile.legalName(), "razao social");
        if (profile.cnpj() == null) {
            throw new IllegalArgumentException("CNPJ e obrigatorio");
        }
        if (profile.industry() == null) {
            throw new IllegalArgumentException("segmento e obrigatorio");
        }
        if (profile.size() == null) {
            throw new IllegalArgumentException("porte e obrigatorio");
        }
        if (profile.type() == null) {
            throw new IllegalArgumentException("tipo de relacionamento e obrigatorio");
        }
        if (profile.employees() != null && profile.employees() < 0) {
            throw new BusinessRuleException("o numero de funcionarios nao pode ser negativo");
        }
        if (profile.annualRevenue() != null && profile.annualRevenue().signum() < 0) {
            throw new BusinessRuleException("o faturamento anual nao pode ser negativo");
        }
        return new CompanyProfile(
                legalName,
                blankToNull(profile.tradeName()),
                profile.cnpj(),
                profile.industry(),
                profile.size(),
                profile.employees(),
                profile.annualRevenue(),
                normalizeWebsite(profile.website()),
                blankToNull(profile.phone()),
                blankToNull(profile.city()),
                profile.state(),
                profile.type(),
                blankToNull(profile.notes())
        );
    }

    private static String normalizeWebsite(String website) {
        String value = blankToNull(website);
        if (value == null) {
            return null;
        }
        String lower = value.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://") ? lower : "https://" + lower;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " e obrigatoria");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String displayName() {
        return profile.tradeName() != null ? profile.tradeName() : profile.legalName();
    }

    public CompanyProfile getProfile() {
        return profile;
    }

    public Long getOwnerId() {
        return ownerId;
    }
}
