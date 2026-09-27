package com.pb.crm.accounts.infrastructure.persistence;

import com.pb.crm.accounts.domain.salesrep.SalesRepRef;
import com.pb.crm.accounts.domain.salesrep.SalesRepRefRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class SalesRepRefRepositoryAdapter implements SalesRepRefRepository {

    private final SpringDataSalesRepRefRepository jpaRepository;
    private final AccountsMapper mapper;

    public SalesRepRefRepositoryAdapter(SpringDataSalesRepRefRepository jpaRepository, AccountsMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<SalesRepRef> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Map<Long, SalesRepRef> findAllByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toMap(SalesRepRef::id, Function.identity()));
    }

    @Override
    public boolean upsert(SalesRepRef salesRep, Instant occurredAt) {
        SalesRepRefJpaEntity entity = jpaRepository.findById(salesRep.id())
                .orElseGet(() -> new SalesRepRefJpaEntity(salesRep.id()));
        if (entity.isStaleComparedTo(occurredAt)) {
            return false;
        }
        entity.apply(salesRep.name(), salesRep.email(), salesRep.active(), salesRep.archived(), occurredAt);
        jpaRepository.save(entity);
        return true;
    }
}
