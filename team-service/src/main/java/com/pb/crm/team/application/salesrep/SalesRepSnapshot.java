package com.pb.crm.team.application.salesrep;

import com.pb.crm.team.domain.salesrep.SalesRep;
import com.pb.crm.team.domain.salesrep.SalesRole;
import com.pb.crm.team.domain.salesrep.SalesTeam;

import java.math.BigDecimal;

public record SalesRepSnapshot(
        Long id,
        String name,
        String email,
        String phone,
        SalesTeam team,
        SalesRole role,
        Long managerId,
        BigDecimal monthlyQuota,
        boolean active,
        boolean archived,
        Long version
) {
    public static SalesRepSnapshot from(SalesRep salesRep) {
        return new SalesRepSnapshot(
                salesRep.getId(),
                salesRep.getName(),
                salesRep.getEmail(),
                salesRep.getPhone(),
                salesRep.getTeam(),
                salesRep.getRole(),
                salesRep.getManagerId(),
                salesRep.getMonthlyQuota(),
                salesRep.isActive(),
                salesRep.isArchived(),
                salesRep.getVersion()
        );
    }
}
