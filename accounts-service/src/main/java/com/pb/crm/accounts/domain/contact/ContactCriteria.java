package com.pb.crm.accounts.domain.contact;

public record ContactCriteria(
        String term,
        Long companyId,
        DecisionRole decisionRole,
        Boolean active,
        boolean includeArchived
) {
}
