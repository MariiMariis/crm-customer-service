package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.sales.domain.opportunity.OpportunityCriteria;
import com.pb.crm.sales.domain.opportunity.OpportunityStage;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class OpportunitySpecifications {

    private static final List<OpportunityStage> CLOSED = List.of(OpportunityStage.WON, OpportunityStage.LOST);

    private OpportunitySpecifications() {
    }

    static Specification<OpportunityJpaEntity> matching(OpportunityCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!criteria.includeArchived()) {
                predicates.add(cb.isFalse(root.get("archived")));
            }
            if (criteria.term() != null && !criteria.term().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + criteria.term().trim().toLowerCase() + "%"));
            }
            if (criteria.stage() != null) {
                predicates.add(cb.equal(root.get("stage"), criteria.stage()));
            }
            if (Boolean.TRUE.equals(criteria.open())) {
                predicates.add(cb.not(root.get("stage").in(CLOSED)));
            } else if (Boolean.FALSE.equals(criteria.open())) {
                predicates.add(root.get("stage").in(CLOSED));
            }
            if (criteria.companyId() != null) {
                predicates.add(cb.equal(root.get("companyId"), criteria.companyId()));
            }
            if (criteria.ownerId() != null) {
                predicates.add(cb.equal(root.get("ownerId"), criteria.ownerId()));
            }
            if (criteria.discountApproval() != null) {
                predicates.add(cb.equal(root.get("discountApproval"), criteria.discountApproval()));
            }
            if (criteria.closingFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expectedCloseDate"), criteria.closingFrom()));
            }
            if (criteria.closingTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expectedCloseDate"), criteria.closingTo()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
