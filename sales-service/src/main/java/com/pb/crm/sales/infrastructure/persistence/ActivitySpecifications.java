package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.activity.ActivityCriteria;
import com.pb.crm.sales.domain.activity.ActivityStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

final class ActivitySpecifications {

    private ActivitySpecifications() {
    }

    static Specification<ActivityJpaEntity> matching(ActivityCriteria criteria, Instant now) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!criteria.includeArchived()) {
                predicates.add(cb.isFalse(root.get("archived")));
            }
            if (criteria.ownerId() != null) {
                predicates.add(cb.equal(root.get("ownerId"), criteria.ownerId()));
            }
            if (criteria.relatedType() != null) {
                predicates.add(cb.equal(root.get("relatedType"), criteria.relatedType()));
            }
            if (criteria.relatedId() != null) {
                predicates.add(cb.equal(root.get("relatedId"), criteria.relatedId()));
            }
            if (criteria.type() != null) {
                predicates.add(cb.equal(root.get("type"), criteria.type()));
            }
            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }
            if (criteria.overdueOnly()) {
                predicates.add(cb.equal(root.get("status"), ActivityStatus.PLANNED));
                predicates.add(cb.lessThan(root.get("dueAt"), now));
            }
            if (criteria.dueFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueAt"), criteria.dueFrom()));
            }
            if (criteria.dueTo() != null) {
                predicates.add(cb.lessThan(root.get("dueAt"), criteria.dueTo()));
            }
            if (criteria.completedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("completedAt"), criteria.completedFrom()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
