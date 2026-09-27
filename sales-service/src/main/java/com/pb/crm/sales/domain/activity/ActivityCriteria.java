package com.pb.crm.sales.domain.activity;

import java.time.Instant;

public record ActivityCriteria(
        Long ownerId,
        RelatedType relatedType,
        Long relatedId,
        ActivityType type,
        ActivityStatus status,
        boolean overdueOnly,
        Instant dueFrom,
        Instant dueTo,
        Instant completedFrom,
        boolean includeArchived
) {
}
