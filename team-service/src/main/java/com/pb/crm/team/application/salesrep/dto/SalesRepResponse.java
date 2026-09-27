package com.pb.crm.team.application.salesrep.dto;

import com.pb.crm.team.domain.salesrep.SalesRep;
import com.pb.crm.team.domain.salesrep.SalesRole;
import com.pb.crm.team.domain.salesrep.SalesTeam;

import java.math.BigDecimal;
import java.time.Instant;

public record SalesRepResponse(
        Long id,
        String name,
        String email,
        String phone,
        SalesTeam team,
        SalesRole role,
        Long managerId,
        String managerName,
        BigDecimal monthlyQuota,
        boolean active,
        boolean archived,
        Instant archivedAt,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        Long version
) {
    public static SalesRepResponse from(SalesRep salesRep, String managerName) {
        return new SalesRepResponse(
                salesRep.getId(),
                salesRep.getName(),
                salesRep.getEmail(),
                salesRep.getPhone(),
                salesRep.getTeam(),
                salesRep.getRole(),
                salesRep.getManagerId(),
                managerName,
                salesRep.getMonthlyQuota(),
                salesRep.isActive(),
                salesRep.isArchived(),
                salesRep.getArchivedAt(),
                salesRep.getAudit().createdAt(),
                salesRep.getAudit().updatedAt(),
                salesRep.getAudit().createdBy(),
                salesRep.getAudit().updatedBy(),
                salesRep.getVersion()
        );
    }

    public static SalesRepResponse from(SalesRep salesRep) {
        return from(salesRep, null);
    }
}
