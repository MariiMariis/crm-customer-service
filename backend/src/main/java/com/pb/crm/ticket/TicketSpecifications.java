package com.pb.crm.ticket;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<Ticket> hasStatus(TicketStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> hasPriority(TicketPriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Ticket> belongsToCustomer(Long customerId) {
        return (root, query, cb) -> customerId == null ? null : cb.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Ticket> assignedTo(Long agentId) {
        return (root, query, cb) -> agentId == null ? null : cb.equal(root.get("agent").get("id"), agentId);
    }

    public static Specification<Ticket> unassigned() {
        return (root, query, cb) -> cb.isNull(root.get("agent"));
    }

    public static Specification<Ticket> subjectContains(String term) {
        return (root, query, cb) -> term == null || term.isBlank()
                ? null
                : cb.like(cb.lower(root.get("subject")), "%" + term.trim().toLowerCase() + "%");
    }

    public static Specification<Ticket> createdBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return null;
            }
            if (from == null) {
                return cb.lessThanOrEqualTo(root.get("createdAt"), to);
            }
            if (to == null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            }
            return cb.between(root.get("createdAt"), from, to);
        };
    }

    public static Specification<Ticket> fetchRelations() {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("customer", JoinType.INNER);
                root.fetch("agent", JoinType.LEFT);
                query.distinct(true);
            }
            return null;
        };
    }

    public static Specification<Ticket> withFilter(TicketFilter filter) {
        return Specification.where(fetchRelations())
                .and(hasStatus(filter.status()))
                .and(hasPriority(filter.priority()))
                .and(belongsToCustomer(filter.customerId()))
                .and(assignedTo(filter.agentId()))
                .and(subjectContains(filter.subject()))
                .and(createdBetween(filter.createdFrom(), filter.createdTo()));
    }
}
