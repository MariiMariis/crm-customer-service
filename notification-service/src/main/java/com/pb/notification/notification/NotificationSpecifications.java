package com.pb.notification.notification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class NotificationSpecifications {

    private NotificationSpecifications() {
    }

    public static Specification<Notification> withFilter(NotificationFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.ticketId() != null) {
                predicates.add(cb.equal(root.get("ticketId"), filter.ticketId()));
            }
            if (filter.customerId() != null) {
                predicates.add(cb.equal(root.get("customerId"), filter.customerId()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.channel() != null) {
                predicates.add(cb.equal(root.get("channel"), filter.channel()));
            }
            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
