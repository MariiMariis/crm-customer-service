package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.reference.SalesRepRef;
import com.pb.crm.sales.domain.reference.SalesRepRefRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class SalesRepRefRepositoryAdapter implements SalesRepRefRepository {

    private final SpringDataSalesRepRefRepository jpaRepository;

    public SalesRepRefRepositoryAdapter(SpringDataSalesRepRefRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<SalesRepRef> findById(Long id) {
        return jpaRepository.findById(id).map(SalesRepRefRepositoryAdapter::toDomain);
    }

    @Override
    public Map<Long, SalesRepRef> findAllByIds(Collection<Long> ids) {
        Map<Long, SalesRepRef> result = new HashMap<>();
        if (!ids.isEmpty()) {
            jpaRepository.findAllById(ids).forEach(entity -> result.put(entity.getId(), toDomain(entity)));
        }
        return result;
    }

    @Override
    public void upsert(SalesRepRef salesRep) {
        SalesRepRefJpaEntity entity = jpaRepository.findById(salesRep.id())
                .orElseGet(() -> new SalesRepRefJpaEntity(salesRep.id()));
        entity.apply(salesRep.name(), salesRep.email(), salesRep.managerId(), salesRep.active(), salesRep.archived());
        jpaRepository.save(entity);
    }

    private static SalesRepRef toDomain(SalesRepRefJpaEntity entity) {
        return new SalesRepRef(entity.getId(), entity.getName(), entity.getEmail(), entity.getManagerId(),
                entity.isActive(), entity.isArchived());
    }
}
