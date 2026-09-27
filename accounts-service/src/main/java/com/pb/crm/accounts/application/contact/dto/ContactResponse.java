package com.pb.crm.accounts.application.contact.dto;

import com.pb.crm.accounts.domain.contact.Contact;
import com.pb.crm.accounts.domain.contact.ContactProfile;
import com.pb.crm.accounts.domain.contact.DecisionRole;

import java.time.Instant;

public record ContactResponse(
        Long id,
        Long companyId,
        String companyName,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String mobile,
        String jobTitle,
        String department,
        DecisionRole decisionRole,
        boolean primary,
        boolean active,
        boolean archived,
        Instant archivedAt,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        Long version
) {
    public static ContactResponse from(Contact contact, String companyName) {
        ContactProfile profile = contact.getProfile();
        return new ContactResponse(
                contact.getId(),
                contact.getCompanyId(),
                companyName,
                profile.firstName(),
                profile.lastName(),
                contact.fullName(),
                profile.email(),
                profile.phone(),
                profile.mobile(),
                profile.jobTitle(),
                profile.department(),
                profile.decisionRole(),
                contact.isPrimary(),
                contact.isActive(),
                contact.isArchived(),
                contact.getArchivedAt(),
                contact.getAudit().createdAt(),
                contact.getAudit().updatedAt(),
                contact.getAudit().createdBy(),
                contact.getAudit().updatedBy(),
                contact.getVersion()
        );
    }

    public static ContactResponse from(Contact contact) {
        return from(contact, null);
    }
}
