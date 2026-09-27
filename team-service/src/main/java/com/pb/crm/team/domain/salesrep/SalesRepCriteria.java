package com.pb.crm.team.domain.salesrep;

public record SalesRepCriteria(
        String term,
        SalesTeam team,
        SalesRole role,
        Boolean active,
        Long managerId,
        boolean includeArchived
) {
}
