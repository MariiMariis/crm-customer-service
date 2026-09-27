package com.pb.crm.sales.domain;

import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.sales.domain.activity.Activity;
import com.pb.crm.sales.domain.activity.ActivityPriority;
import com.pb.crm.sales.domain.activity.ActivitySchedule;
import com.pb.crm.sales.domain.activity.ActivityStatus;
import com.pb.crm.sales.domain.activity.ActivityType;
import com.pb.crm.sales.domain.activity.RelatedTo;
import com.pb.crm.sales.domain.activity.RelatedType;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityTest {

    private static final SalesRepRef ANA = new SalesRepRef(1L, "Ana", null, null, true, false);
    private static final RelatedTo OPPORTUNITY = new RelatedTo(RelatedType.OPPORTUNITY, 42L);

    private static Activity call(Instant dueAt) {
        return Activity.plan(ActivityType.CALL, "Retorno sobre a proposta", null, null, OPPORTUNITY, ANA,
                ActivitySchedule.deadline(dueAt));
    }

    @Test
    void planDefaultsPriorityAndComputesOverdue() {
        Instant now = Instant.now();
        Activity activity = call(now.minus(Duration.ofHours(2)));

        assertThat(activity.getPriority()).isEqualTo(ActivityPriority.NORMAL);
        assertThat(activity.getStatus()).isEqualTo(ActivityStatus.PLANNED);
        assertThat(activity.isOverdue(now)).isTrue();
        assertThat(call(now.plus(Duration.ofDays(1))).isOverdue(now)).isFalse();
    }

    @Test
    void meetingRequiresValidWindowUpToEightHours() {
        Instant start = Instant.parse("2026-10-05T13:00:00Z");

        assertThatThrownBy(() -> Activity.plan(ActivityType.MEETING, "Demo", null, null, OPPORTUNITY, ANA,
                ActivitySchedule.meeting(start, start.minusSeconds(60), null))).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> Activity.plan(ActivityType.MEETING, "Workshop", null, null, OPPORTUNITY, ANA,
                ActivitySchedule.meeting(start, start.plus(Duration.ofHours(9)), null))).isInstanceOf(BusinessRuleException.class);

        Activity meeting = Activity.plan(ActivityType.MEETING, "Demonstracao do ERP", null, ActivityPriority.HIGH,
                OPPORTUNITY, ANA, ActivitySchedule.meeting(start, start.plus(Duration.ofHours(1)), " https://meet.pbtech/erp "));
        assertThat(meeting.getSchedule().dueAt()).isEqualTo(start);
        assertThat(meeting.getSchedule().location()).isEqualTo("https://meet.pbtech/erp");
    }

    @Test
    void callCompletionRequiresOutcomeAndDuration() {
        Activity activity = call(Instant.now());

        assertThatThrownBy(() -> activity.complete(null, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> activity.complete("cliente pediu nova proposta", null)).isInstanceOf(IllegalArgumentException.class);

        activity.complete("cliente pediu nova proposta", 12);
        assertThat(activity.getStatus()).isEqualTo(ActivityStatus.DONE);
        assertThat(activity.getCompletedAt()).isNotNull();
        assertThat(activity.isOverdue(Instant.now().plusSeconds(86400))).isFalse();
    }

    @Test
    void taskCompletesWithoutOutcomeAndCanBeReopened() {
        Activity task = Activity.plan(ActivityType.TASK, "Enviar proposta revisada", null, null, OPPORTUNITY, ANA,
                ActivitySchedule.deadline(Instant.now()));

        task.complete(null, null);
        assertThatThrownBy(() -> task.cancel("duplicada")).isInstanceOf(BusinessRuleException.class);

        task.reopen();
        assertThat(task.getStatus()).isEqualTo(ActivityStatus.PLANNED);
        assertThat(task.getCompletedAt()).isNull();
    }

    @Test
    void cancellationRequiresReasonAndInactiveOwnerIsRejected() {
        Activity activity = call(Instant.now());
        assertThatThrownBy(() -> activity.cancel(" ")).isInstanceOf(IllegalArgumentException.class);
        activity.cancel("cliente remarcou");
        assertThat(activity.getCancelReason()).isEqualTo("cliente remarcou");

        SalesRepRef inactive = new SalesRepRef(2L, "Caio", null, null, false, false);
        assertThatThrownBy(() -> Activity.plan(ActivityType.EMAIL, "Follow-up", null, null, OPPORTUNITY, inactive,
                ActivitySchedule.deadline(Instant.now()))).isInstanceOf(BusinessRuleException.class);
    }
}
