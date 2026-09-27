package com.pb.crm.accounts.application.conversion;

public final class LeadAccountOutcome {

    public static final String PROVISIONED = "lead-account.provisioned";
    public static final String REJECTED = "lead-account.rejected";

    private LeadAccountOutcome() {
    }

    public record Provisioned(
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

    public record Rejected(
            Long leadId,
            String reason
    ) {
    }
}
