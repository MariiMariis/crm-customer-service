package com.pb.crm.sales.domain.activity;

import com.pb.crm.commons.domain.AggregateRoot;
import com.pb.crm.commons.domain.AuditInfo;
import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.sales.domain.reference.SalesRepRef;

import java.time.Instant;

public class Activity extends AggregateRoot {

    private ActivityType type;
    private String subject;
    private String description;
    private ActivityPriority priority;
    private RelatedTo relatedTo;
    private Long ownerId;
    private ActivitySchedule schedule;
    private ActivityStatus status;
    private String outcome;
    private Integer durationMinutes;
    private String cancelReason;
    private Instant completedAt;

    private Activity() {
    }

    public static Activity plan(ActivityType type,
                                String subject,
                                String description,
                                ActivityPriority priority,
                                RelatedTo relatedTo,
                                SalesRepRef owner,
                                ActivitySchedule schedule) {
        if (type == null) {
            throw new IllegalArgumentException("tipo da atividade e obrigatorio");
        }
        if (relatedTo == null) {
            throw new IllegalArgumentException("registro relacionado e obrigatorio");
        }
        Activity activity = new Activity();
        activity.type = type;
        activity.relatedTo = relatedTo;
        activity.status = ActivityStatus.PLANNED;
        activity.applyDetails(subject, description, priority, schedule);
        activity.assignOwner(owner);
        return activity;
    }

    public static Activity rehydrate(Long id,
                                     ActivityType type,
                                     String subject,
                                     String description,
                                     ActivityPriority priority,
                                     RelatedTo relatedTo,
                                     Long ownerId,
                                     ActivitySchedule schedule,
                                     ActivityStatus status,
                                     String outcome,
                                     Integer durationMinutes,
                                     String cancelReason,
                                     Instant completedAt,
                                     boolean archived,
                                     Instant archivedAt,
                                     AuditInfo audit) {
        Activity activity = new Activity();
        activity.rehydrateBase(id, audit, archived, archivedAt);
        activity.type = type;
        activity.subject = subject;
        activity.description = description;
        activity.priority = priority;
        activity.relatedTo = relatedTo;
        activity.ownerId = ownerId;
        activity.schedule = schedule;
        activity.status = status;
        activity.outcome = outcome;
        activity.durationMinutes = durationMinutes;
        activity.cancelReason = cancelReason;
        activity.completedAt = completedAt;
        return activity;
    }

    public void reschedule(String subject, String description, ActivityPriority priority, ActivitySchedule schedule) {
        assertPlanned("editar");
        applyDetails(subject, description, priority, schedule);
    }

    public void reassign(SalesRepRef owner) {
        assertPlanned("reatribuir");
        assignOwner(owner);
    }

    public void complete(String outcome, Integer durationMinutes) {
        assertPlanned("concluir");
        String result = outcome == null || outcome.isBlank() ? null : outcome.trim();
        if (type.requiresOutcome() && result == null) {
            throw new IllegalArgumentException("informe o resultado da %s".formatted(type == ActivityType.CALL ? "ligacao" : "reuniao"));
        }
        if (type == ActivityType.CALL && (durationMinutes == null || durationMinutes < 1)) {
            throw new IllegalArgumentException("informe a duracao da ligacao em minutos");
        }
        if (durationMinutes != null && durationMinutes < 0) {
            throw new BusinessRuleException("a duracao nao pode ser negativa");
        }
        this.status = ActivityStatus.DONE;
        this.outcome = result;
        this.durationMinutes = durationMinutes;
        this.completedAt = Instant.now();
    }

    public void cancel(String reason) {
        assertPlanned("cancelar");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("o motivo do cancelamento e obrigatorio");
        }
        this.status = ActivityStatus.CANCELED;
        this.cancelReason = reason.trim();
    }

    public void reopen() {
        assertNotArchived();
        if (status == ActivityStatus.PLANNED) {
            throw new BusinessRuleException("a atividade ja esta planejada");
        }
        this.status = ActivityStatus.PLANNED;
        this.outcome = null;
        this.durationMinutes = null;
        this.cancelReason = null;
        this.completedAt = null;
    }

    public boolean isOverdue(Instant now) {
        return status == ActivityStatus.PLANNED && !isArchived() && schedule.dueAt().isBefore(now);
    }

    private void applyDetails(String subject, String description, ActivityPriority priority, ActivitySchedule schedule) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("assunto e obrigatorio");
        }
        if (schedule == null) {
            throw new IllegalArgumentException("agenda da atividade e obrigatoria");
        }
        this.subject = subject.trim();
        this.description = description == null || description.isBlank() ? null : description.trim();
        this.priority = priority == null ? ActivityPriority.NORMAL : priority;
        this.schedule = schedule.validateFor(type);
    }

    private void assignOwner(SalesRepRef owner) {
        if (owner == null) {
            throw new IllegalArgumentException("responsavel e obrigatorio");
        }
        if (!owner.canOwnRecords()) {
            throw new BusinessRuleException("o responsavel informado esta inativo ou arquivado");
        }
        this.ownerId = owner.id();
    }

    private void assertPlanned(String action) {
        assertNotArchived();
        if (status != ActivityStatus.PLANNED) {
            throw new BusinessRuleException("nao e possivel %s uma atividade com status %s".formatted(action, status));
        }
    }

    public ActivityType getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public ActivityPriority getPriority() {
        return priority;
    }

    public RelatedTo getRelatedTo() {
        return relatedTo;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public ActivitySchedule getSchedule() {
        return schedule;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public String getOutcome() {
        return outcome;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
