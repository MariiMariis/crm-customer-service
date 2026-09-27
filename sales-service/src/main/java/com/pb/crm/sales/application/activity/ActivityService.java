package com.pb.crm.sales.application.activity;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.sales.application.activity.dto.ActivityRequest;
import com.pb.crm.sales.application.activity.dto.ActivityResponse;
import com.pb.crm.sales.application.activity.dto.ActivitySummaryResponse;
import com.pb.crm.sales.domain.activity.ActivityCriteria;

import java.time.Instant;
import java.util.List;

public interface ActivityService {

    ActivityResponse create(ActivityRequest request);

    ActivityResponse update(Long id, ActivityRequest request);

    ActivityResponse findById(Long id);

    PageResult<ActivityResponse> search(ActivityCriteria criteria, PageQuery page);

    List<ActivityResponse> agenda(Long ownerId, Instant from, Instant to);

    ActivitySummaryResponse summary(Long ownerId);

    ActivityResponse complete(Long id, String outcome, Integer durationMinutes);

    ActivityResponse cancel(Long id, String reason);

    ActivityResponse reopen(Long id);

    ActivityResponse archive(Long id);

    ActivityResponse restore(Long id);

    List<AuditRevision<ActivityResponse>> findRevisions(Long id);
}
