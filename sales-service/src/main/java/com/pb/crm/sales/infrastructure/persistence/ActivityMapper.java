package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.activity.Activity;
import com.pb.crm.sales.domain.activity.ActivitySchedule;
import com.pb.crm.sales.domain.activity.RelatedTo;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public Activity toDomain(ActivityJpaEntity entity) {
        return Activity.rehydrate(
                entity.getId(),
                entity.getType(),
                entity.getSubject(),
                entity.getDescription(),
                entity.getPriority(),
                new RelatedTo(entity.getRelatedType(), entity.getRelatedId()),
                entity.getOwnerId(),
                new ActivitySchedule(entity.getDueAt(), entity.getStartsAt(), entity.getEndsAt(), entity.getLocation()),
                entity.getStatus(),
                entity.getOutcome(),
                entity.getDurationMinutes(),
                entity.getCancelReason(),
                entity.getCompletedAt(),
                entity.isArchived(),
                entity.getArchivedAt(),
                entity.toAuditInfo()
        );
    }

    public void copyToEntity(Activity activity, ActivityJpaEntity entity) {
        entity.setType(activity.getType());
        entity.setSubject(activity.getSubject());
        entity.setDescription(activity.getDescription());
        entity.setPriority(activity.getPriority());
        entity.setRelatedType(activity.getRelatedTo().type());
        entity.setRelatedId(activity.getRelatedTo().id());
        entity.setOwnerId(activity.getOwnerId());
        entity.setDueAt(activity.getSchedule().dueAt());
        entity.setStartsAt(activity.getSchedule().startsAt());
        entity.setEndsAt(activity.getSchedule().endsAt());
        entity.setLocation(activity.getSchedule().location());
        entity.setStatus(activity.getStatus());
        entity.setOutcome(activity.getOutcome());
        entity.setDurationMinutes(activity.getDurationMinutes());
        entity.setCancelReason(activity.getCancelReason());
        entity.setCompletedAt(activity.getCompletedAt());
        entity.applyArchiveState(activity.isArchived(), activity.getArchivedAt());
    }
}
