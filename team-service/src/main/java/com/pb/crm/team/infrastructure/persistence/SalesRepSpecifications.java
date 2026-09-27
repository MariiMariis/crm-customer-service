package com.pb.crm.team.infrastructure.persistence;

import com.pb.crm.team.domain.salesrep.SalesRepCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class SalesRepSpecifications {

    private SalesRepSpecifications() {
    }

    static Specification<SalesRepJpaEntity> matching(SalesRepCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!criteria.includeArchived()) {
                predicates.add(cb.isFalse(root.get("archived")));
            }
            if (criteria.term() != null && !criteria.term().isBlank()) {
                String like = "%" + criteria.term().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("email")), like)
                ));
            }
            if (criteria.team() != null) {
                predicates.add(cb.equal(root.get("team"), criteria.team()));
            }
            if (criteria.role() != null) {
                predicates.add(cb.equal(root.get("role"), criteria.role()));
            }
            if (criteria.active() != null) {
                predicates.add(cb.equal(root.get("active"), criteria.active()));
            }
            if (criteria.managerId() != null) {
                predicates.add(cb.equal(root.get("managerId"), criteria.managerId()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
