package com.pb.crm.team.infrastructure.persistence;

import com.pb.crm.team.domain.salesrep.SalesRep;
import org.springframework.stereotype.Component;

@Component
public class SalesRepMapper {

    public SalesRep toDomain(SalesRepJpaEntity entity) {
        return SalesRep.rehydrate(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getTeam(),
                entity.getRole(),
                entity.getManagerId(),
                entity.getMonthlyQuota(),
                entity.isActive(),
                entity.isArchived(),
                entity.getArchivedAt(),
                entity.toAuditInfo()
        );
    }

    public void copyToEntity(SalesRep salesRep, SalesRepJpaEntity entity) {
        entity.setName(salesRep.getName());
        entity.setEmail(salesRep.getEmail());
        entity.setPhone(salesRep.getPhone());
        entity.setTeam(salesRep.getTeam());
        entity.setRole(salesRep.getRole());
        entity.setManagerId(salesRep.getManagerId());
        entity.setMonthlyQuota(salesRep.getMonthlyQuota());
        entity.setActive(salesRep.isActive());
        entity.applyArchiveState(salesRep.isArchived(), salesRep.getArchivedAt());
    }
}
