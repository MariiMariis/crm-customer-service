package com.pb.crm.sales.domain.reference;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface SalesRepRefRepository {

    Optional<SalesRepRef> findById(Long id);

    Map<Long, SalesRepRef> findAllByIds(Collection<Long> ids);

    boolean upsert(SalesRepRef salesRep, Instant occurredAt);

    default void upsert(SalesRepRef salesRep) {
        upsert(salesRep, Instant.now());
    }
}
