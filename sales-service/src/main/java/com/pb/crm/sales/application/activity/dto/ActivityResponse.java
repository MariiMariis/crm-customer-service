package com.pb.crm.sales.application.activity.dto;

import com.pb.crm.sales.domain.activity.Activity;
import com.pb.crm.sales.domain.activity.ActivityPriority;
import com.pb.crm.sales.domain.activity.ActivityStatus;
import com.pb.crm.sales.domain.activity.ActivityType;
import com.pb.crm.sales.domain.activity.RelatedType;

import java.time.Instant;

public record ActivityResponse(
        Long id,
        ActivityType type,
        String subject,
        String description,
        ActivityPriority priority,
        RelatedType relatedType,
        Long relatedId,
        String relatedName,
        Long ownerId,
        String ownerName,
        Instant dueAt,
        Instant startsAt,
        Instant endsAt,
        String location,
        ActivityStatus status,
        boolean overdue,
        String outcome,
        Integer durationMinutes,
        String cancelReason,
        Instant completedAt,
        boolean archived,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        Long version
) {
    public static ActivityResponse from(Activity activity, String relatedName, String ownerName, Instant now) {
        return new ActivityResponse(
                activity.getId(),
                activity.getType(),
                activity.getSubject(),
                activity.getDescription(),
                activity.getPriority(),
                activity.getRelatedTo().type(),
                activity.getRelatedTo().id(),
                relatedName,
                activity.getOwnerId(),
                ownerName,
                activity.getSchedule().dueAt(),
                activity.getSchedule().startsAt(),
                activity.getSchedule().endsAt(),
                activity.getSchedule().location(),
                activity.getStatus(),
                activity.isOverdue(now),
                activity.getOutcome(),
                activity.getDurationMinutes(),
                activity.getCancelReason(),
                activity.getCompletedAt(),
                activity.isArchived(),
                activity.getAudit().createdAt(),
                activity.getAudit().updatedAt(),
                activity.getAudit().createdBy(),
                activity.getAudit().updatedBy(),
                activity.getVersion()
        );
    }
}
