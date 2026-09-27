package com.pb.crm.accounts.application.events;

import com.pb.crm.accounts.domain.contact.Contact;
import com.pb.crm.accounts.domain.contact.ContactProfile;
import com.pb.crm.accounts.domain.contact.DecisionRole;

public record ContactSnapshot(
        Long id,
        Long companyId,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String jobTitle,
        DecisionRole decisionRole,
        boolean primary,
        boolean active,
        boolean archived,
        Long version
) {
    public static ContactSnapshot from(Contact contact) {
        ContactProfile profile = contact.getProfile();
        return new ContactSnapshot(
                contact.getId(),
                contact.getCompanyId(),
                profile.firstName(),
                profile.lastName(),
                contact.fullName(),
                profile.email(),
                profile.phone(),
                profile.jobTitle(),
                profile.decisionRole(),
                contact.isPrimary(),
                contact.isActive(),
                contact.isArchived(),
                contact.getVersion()
        );
    }
}
