package com.pb.crm.sales.domain.lead;

public record LeadCriteria(
        String term,
        LeadStatus status,
        LeadSource source,
        Long ownerId,
        boolean unassignedOnly,
        Integer minScore,
        boolean includeArchived
) {
}
