package com.pb.crm.sales.infrastructure.messaging;

public final class AccountsPayloads {

    private AccountsPayloads() {
    }

    public record Company(
            Long id,
            String displayName,
            String cnpj,
            Long ownerId,
            boolean archived
    ) {
    }

    public record Contact(
            Long id,
            Long companyId,
            String fullName,
            String email,
            boolean active,
            boolean archived
    ) {
    }

    public record LeadAccountProvisioned(
            Long leadId,
            Long companyId,
            String companyName,
            String cnpj,
            Long companyOwnerId,
            boolean companyCreated,
            Long contactId,
            String contactName,
            String contactEmail,
            boolean contactCreated
    ) {
    }

    public record LeadAccountRejected(
            Long leadId,
            String reason
    ) {
    }
}
