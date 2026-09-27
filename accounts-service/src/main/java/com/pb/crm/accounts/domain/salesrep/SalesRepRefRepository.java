package com.pb.crm.accounts.domain.salesrep;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface SalesRepRefRepository {

    Optional<SalesRepRef> findById(Long id);

    Map<Long, SalesRepRef> findAllByIds(Collection<Long> ids);

    void upsert(SalesRepRef salesRep);
}
