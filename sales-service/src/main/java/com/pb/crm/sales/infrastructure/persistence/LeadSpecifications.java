package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.lead.LeadCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class LeadSpecifications {

    private LeadSpecifications() {
    }

    static Specification<LeadJpaEntity> matching(LeadCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!criteria.includeArchived()) {
                predicates.add(cb.isFalse(root.get("archived")));
            }
            if (criteria.term() != null && !criteria.term().isBlank()) {
                String like = "%" + criteria.term().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("companyName")), like)
                ));
            }
            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }
            if (criteria.source() != null) {
                predicates.add(cb.equal(root.get("source"), criteria.source()));
            }
            if (criteria.unassignedOnly()) {
                predicates.add(cb.isNull(root.get("ownerId")));
            } else if (criteria.ownerId() != null) {
                predicates.add(cb.equal(root.get("ownerId"), criteria.ownerId()));
            }
            if (criteria.minScore() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("score"), criteria.minScore()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
