package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.commons.audit.AuditRevisions;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.sales.domain.activity.Activity;
import com.pb.crm.sales.domain.activity.ActivityCriteria;
import com.pb.crm.sales.domain.activity.ActivityRepository;
import com.pb.crm.sales.domain.activity.ActivityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class ActivityRepositoryAdapter implements ActivityRepository {

    private static final Sort BY_DUE_DATE = Sort.by("dueAt").ascending().and(Sort.by("id"));

    private final SpringDataActivityRepository jpaRepository;
    private final ActivityMapper mapper;

    public ActivityRepositoryAdapter(SpringDataActivityRepository jpaRepository, ActivityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Activity save(Activity activity) {
        ActivityJpaEntity entity = activity.isNew()
                ? new ActivityJpaEntity()
                : jpaRepository.findById(activity.getId())
                        .orElseThrow(() -> ResourceNotFoundException.forId("Atividade", activity.getId()));
        mapper.copyToEntity(activity, entity);
        return mapper.toDomain(jpaRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<Activity> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Activity> search(ActivityCriteria criteria, Instant now, PageQuery page) {
        Page<ActivityJpaEntity> result = jpaRepository.findAll(
                ActivitySpecifications.matching(criteria, now), PageRequest.of(page.page(), page.size(), BY_DUE_DATE));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    @Override
    public List<Activity> findAgenda(Long ownerId, Instant from, Instant to) {
        ActivityCriteria criteria = new ActivityCriteria(ownerId, null, null, null, ActivityStatus.PLANNED, false, from, to, null, false);
        return jpaRepository.findAll(ActivitySpecifications.matching(criteria, from), BY_DUE_DATE).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long count(ActivityCriteria criteria, Instant now) {
        return jpaRepository.count(ActivitySpecifications.matching(criteria, now));
    }

    @Override
    public List<AuditRevision<Activity>> findRevisions(Long id) {
        return jpaRepository.findRevisions(id).stream()
                .map(revision -> AuditRevisions.from(revision, mapper::toDomain))
                .toList();
    }
}
