package com.pb.crm.sales.domain.activity;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository {

    Activity save(Activity activity);

    Optional<Activity> findById(Long id);

    PageResult<Activity> search(ActivityCriteria criteria, Instant now, PageQuery page);

    List<Activity> findAgenda(Long ownerId, Instant from, Instant to);

    long count(ActivityCriteria criteria, Instant now);

    List<AuditRevision<Activity>> findRevisions(Long id);
}
