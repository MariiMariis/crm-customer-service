package com.pb.crm.sales.domain.activity;

import com.pb.crm.commons.error.BusinessRuleException;

import java.time.Duration;
import java.time.Instant;

public record ActivitySchedule(
        Instant dueAt,
        Instant startsAt,
        Instant endsAt,
        String location
) {
    public static final Duration MAX_MEETING_DURATION = Duration.ofHours(8);

    public static ActivitySchedule deadline(Instant dueAt) {
        return new ActivitySchedule(dueAt, null, null, null);
    }

    public static ActivitySchedule meeting(Instant startsAt, Instant endsAt, String location) {
        return new ActivitySchedule(startsAt, startsAt, endsAt, location);
    }

    ActivitySchedule validateFor(ActivityType type) {
        if (type == ActivityType.MEETING) {
            if (startsAt == null || endsAt == null) {
                throw new IllegalArgumentException("reunioes exigem inicio e fim");
            }
            if (!endsAt.isAfter(startsAt)) {
                throw new BusinessRuleException("o fim da reuniao deve ser posterior ao inicio");
            }
            if (Duration.between(startsAt, endsAt).compareTo(MAX_MEETING_DURATION) > 0) {
                throw new BusinessRuleException("uma reuniao pode durar no maximo 8 horas");
            }
            String place = location == null || location.isBlank() ? null : location.trim();
            return new ActivitySchedule(startsAt, startsAt, endsAt, place);
        }
        if (dueAt == null) {
            throw new IllegalArgumentException("prazo e obrigatorio");
        }
        return new ActivitySchedule(dueAt, null, null, null);
    }
}
