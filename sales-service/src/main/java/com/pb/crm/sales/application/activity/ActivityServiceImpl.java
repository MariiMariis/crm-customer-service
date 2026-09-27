package com.pb.crm.sales.application.activity;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.commons.messaging.DomainEventPublisher;
import com.pb.crm.sales.application.events.SalesEventPayloads;
import com.pb.crm.sales.application.activity.dto.ActivityRequest;
import com.pb.crm.sales.application.activity.dto.ActivityResponse;
import com.pb.crm.sales.application.activity.dto.ActivitySummaryResponse;
import com.pb.crm.sales.domain.activity.Activity;
import com.pb.crm.sales.domain.activity.ActivityCriteria;
import com.pb.crm.sales.domain.activity.ActivityRepository;
import com.pb.crm.sales.domain.activity.ActivitySchedule;
import com.pb.crm.sales.domain.activity.ActivityStatus;
import com.pb.crm.sales.domain.activity.ActivityType;
import com.pb.crm.sales.domain.activity.RelatedTo;
import com.pb.crm.sales.domain.reference.SalesRepRef;
import com.pb.crm.sales.domain.reference.SalesRepRefRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final Duration MAX_AGENDA_RANGE = Duration.ofDays(92);

    private final ActivityRepository activityRepository;
    private final SalesRepRefRepository salesRepRefRepository;
    private final RelatedRecordResolver relatedRecordResolver;
    private final DomainEventPublisher eventPublisher;

    public ActivityServiceImpl(ActivityRepository activityRepository,
                               SalesRepRefRepository salesRepRefRepository,
                               RelatedRecordResolver relatedRecordResolver,
                               DomainEventPublisher eventPublisher) {
        this.activityRepository = activityRepository;
        this.salesRepRefRepository = salesRepRefRepository;
        this.relatedRecordResolver = relatedRecordResolver;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ActivityResponse create(ActivityRequest request) {
        RelatedTo relatedTo = new RelatedTo(request.relatedType(), request.relatedId());
        relatedRecordResolver.requireActive(relatedTo);
        Activity activity = Activity.plan(request.type(), request.subject(), request.description(), request.priority(),
                relatedTo, loadOwner(request.ownerId()), toSchedule(request));
        return toResponse(persist(activity));
    }

    @Override
    @Transactional
    public ActivityResponse update(Long id, ActivityRequest request) {
        Activity activity = load(id);
        if (request.version() != null && !request.version().equals(activity.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Activity.class, id);
        }
        if (request.type() != activity.getType()
                || request.relatedType() != activity.getRelatedTo().type()
                || !request.relatedId().equals(activity.getRelatedTo().id())) {
            throw new BusinessRuleException("o tipo e o registro relacionado de uma atividade nao podem ser alterados");
        }
        activity.reschedule(request.subject(), request.description(), request.priority(), toSchedule(request));
        if (!activity.getOwnerId().equals(request.ownerId())) {
            activity.reassign(loadOwner(request.ownerId()));
        }
        return toResponse(persist(activity));
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse findById(Long id) {
        return toResponse(load(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ActivityResponse> search(ActivityCriteria criteria, PageQuery page) {
        Instant now = Instant.now();
        PageResult<Activity> result = activityRepository.search(criteria, now, page);
        Map<Long, SalesRepRef> owners = ownersOf(result.content());
        return result.map(activity -> toResponse(activity, owners, now));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> agenda(Long ownerId, Instant from, Instant to) {
        if (from == null || to == null || !to.isAfter(from)) {
            throw new IllegalArgumentException("informe um periodo valido (from < to)");
        }
        if (Duration.between(from, to).compareTo(MAX_AGENDA_RANGE) > 0) {
            throw new IllegalArgumentException("o periodo da agenda pode ter no maximo 92 dias");
        }
        Instant now = Instant.now();
        List<Activity> activities = activityRepository.findAgenda(ownerId, from, to);
        Map<Long, SalesRepRef> owners = ownersOf(activities);
        return activities.stream().map(activity -> toResponse(activity, owners, now)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivitySummaryResponse summary(Long ownerId) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        Instant startOfToday = today.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant startOfTomorrow = today.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant inSevenDays = today.plusDays(8).atStartOfDay(BUSINESS_ZONE).toInstant();
        long overdue = activityRepository.count(
                new ActivityCriteria(ownerId, null, null, null, null, true, null, null, null, false), now);
        long dueToday = activityRepository.count(
                new ActivityCriteria(ownerId, null, null, null, ActivityStatus.PLANNED, false, startOfToday, startOfTomorrow, null, false), now);
        long next7Days = activityRepository.count(
                new ActivityCriteria(ownerId, null, null, null, ActivityStatus.PLANNED, false, startOfTomorrow, inSevenDays, null, false), now);
        long doneLast7Days = activityRepository.count(
                new ActivityCriteria(ownerId, null, null, null, ActivityStatus.DONE, false, null, null,
                        today.minusDays(7).atStartOfDay(BUSINESS_ZONE).toInstant(), false), now);
        return new ActivitySummaryResponse(overdue, dueToday, next7Days, doneLast7Days);
    }

    @Override
    @Transactional
    public ActivityResponse complete(Long id, String outcome, Integer durationMinutes) {
        Activity activity = load(id);
        activity.complete(outcome, durationMinutes);
        return toResponse(persist(activity));
    }

    @Override
    @Transactional
    public ActivityResponse cancel(Long id, String reason) {
        Activity activity = load(id);
        activity.cancel(reason);
        return toResponse(persist(activity));
    }

    @Override
    @Transactional
    public ActivityResponse reopen(Long id) {
        Activity activity = load(id);
        activity.reopen();
        return toResponse(persist(activity));
    }

    @Override
    @Transactional
    public ActivityResponse archive(Long id) {
        Activity activity = load(id);
        activity.archive();
        return toResponse(persist(activity));
    }

    @Override
    @Transactional
    public ActivityResponse restore(Long id) {
        Activity activity = load(id);
        activity.restore();
        return toResponse(persist(activity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRevision<ActivityResponse>> findRevisions(Long id) {
        load(id);
        Instant now = Instant.now();
        return activityRepository.findRevisions(id).stream()
                .map(revision -> revision.map(activity -> ActivityResponse.from(activity, null, null, now)))
                .toList();
    }

    private Activity persist(Activity activity) {
        List<String> events = activity.pullEvents();
        Activity saved = activityRepository.save(activity);
        if (!events.isEmpty()) {
            SalesRepRef owner = salesRepRefRepository.findById(saved.getOwnerId()).orElse(null);
            SalesEventPayloads.ActivityPayload payload = SalesEventPayloads.ActivityPayload.from(
                    saved, relatedRecordResolver.nameOf(saved.getRelatedTo()), owner);
            events.forEach(event -> eventPublisher.publish("sales." + event, "Activity", saved.getId(), payload));
        }
        return saved;
    }

    private Activity load(Long id) {
        return activityRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Atividade", id));
    }

    private SalesRepRef loadOwner(Long ownerId) {
        return salesRepRefRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessRuleException(
                        "responsavel %d nao encontrado na base sincronizada da equipe comercial".formatted(ownerId)));
    }

    private static ActivitySchedule toSchedule(ActivityRequest request) {
        return request.type() == ActivityType.MEETING
                ? ActivitySchedule.meeting(request.startsAt(), request.endsAt(), request.location())
                : ActivitySchedule.deadline(request.dueAt());
    }

    private Map<Long, SalesRepRef> ownersOf(List<Activity> activities) {
        Set<Long> ownerIds = activities.stream().map(Activity::getOwnerId).collect(Collectors.toSet());
        return salesRepRefRepository.findAllByIds(ownerIds);
    }

    private ActivityResponse toResponse(Activity activity) {
        return toResponse(activity, ownersOf(List.of(activity)), Instant.now());
    }

    private ActivityResponse toResponse(Activity activity, Map<Long, SalesRepRef> owners, Instant now) {
        SalesRepRef owner = owners.get(activity.getOwnerId());
        return ActivityResponse.from(activity, relatedRecordResolver.nameOf(activity.getRelatedTo()),
                owner == null ? null : owner.name(), now);
    }
}
