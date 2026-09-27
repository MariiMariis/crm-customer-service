package com.pb.crm.accounts.domain.contact;

import com.pb.crm.accounts.domain.company.Company;
import com.pb.crm.commons.domain.AggregateRoot;
import com.pb.crm.commons.domain.AuditInfo;
import com.pb.crm.commons.error.BusinessRuleException;

import java.time.Instant;

public class Contact extends AggregateRoot {

    private Long companyId;
    private ContactProfile profile;
    private boolean primary;
    private boolean active;

    private Contact() {
    }

    public static Contact register(Company company, ContactProfile profile, boolean primary) {
        if (company == null || company.isNew()) {
            throw new IllegalArgumentException("empresa e obrigatoria");
        }
        if (!company.acceptsNewContacts()) {
            throw new BusinessRuleException("nao e possivel adicionar contatos a uma empresa arquivada");
        }
        Contact contact = new Contact();
        contact.companyId = company.getId();
        contact.profile = validate(profile);
        contact.active = true;
        contact.primary = primary;
        return contact;
    }

    public static Contact rehydrate(Long id,
                                    Long companyId,
                                    ContactProfile profile,
                                    boolean primary,
                                    boolean active,
                                    boolean archived,
                                    Instant archivedAt,
                                    AuditInfo audit) {
        Contact contact = new Contact();
        contact.rehydrateBase(id, audit, archived, archivedAt);
        contact.companyId = companyId;
        contact.profile = profile;
        contact.primary = primary;
        contact.active = active;
        return contact;
    }

    public void update(ContactProfile profile, boolean active) {
        assertNotArchived();
        this.profile = validate(profile);
        this.active = active;
        if (!active) {
            this.primary = false;
        }
    }

    public void makePrimary() {
        assertNotArchived();
        if (!active) {
            throw new BusinessRuleException("um contato inativo nao pode ser o contato principal");
        }
        if (primary) {
            throw new BusinessRuleException("o contato ja e o principal da empresa");
        }
        primary = true;
    }

    public void unmarkPrimary() {
        primary = false;
    }

    @Override
    public void archive() {
        super.archive();
        primary = false;
    }

    private static ContactProfile validate(ContactProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("dados do contato sao obrigatorios");
        }
        if (profile.decisionRole() == null) {
            throw new IllegalArgumentException("papel na decisao e obrigatorio");
        }
        return new ContactProfile(
                requireText(profile.firstName(), "nome"),
                requireText(profile.lastName(), "sobrenome"),
                requireText(profile.email(), "email").toLowerCase(),
                blankToNull(profile.phone()),
                blankToNull(profile.mobile()),
                blankToNull(profile.jobTitle()),
                blankToNull(profile.department()),
                profile.decisionRole()
        );
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " e obrigatorio");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String fullName() {
        return profile.firstName() + " " + profile.lastName();
    }

    public Long getCompanyId() {
        return companyId;
    }

    public ContactProfile getProfile() {
        return profile;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isActive() {
        return active;
    }
}
