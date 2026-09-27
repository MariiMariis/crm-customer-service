package com.pb.crm.accounts.infrastructure.persistence;

import com.pb.crm.accounts.domain.company.CompanyCriteria;
import com.pb.crm.accounts.domain.contact.ContactCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class AccountsSpecifications {

    private AccountsSpecifications() {
    }

    static Specification<CompanyJpaEntity> companies(CompanyCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!criteria.includeArchived()) {
                predicates.add(cb.isFalse(root.get("archived")));
            }
            if (hasText(criteria.term())) {
                String term = criteria.term().trim().toLowerCase();
                String like = "%" + term + "%";
                String digits = term.replaceAll("[.\\-/\\s]", "").toUpperCase();
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("legalName")), like),
                        cb.like(cb.lower(root.get("tradeName")), like),
                        cb.like(root.get("cnpj"), "%" + digits + "%")
                ));
            }
            if (criteria.industry() != null) {
                predicates.add(cb.equal(root.get("industry"), criteria.industry()));
            }
            if (criteria.size() != null) {
                predicates.add(cb.equal(root.get("size"), criteria.size()));
            }
            if (criteria.type() != null) {
                predicates.add(cb.equal(root.get("type"), criteria.type()));
            }
            if (criteria.state() != null) {
                predicates.add(cb.equal(root.get("state"), criteria.state()));
            }
            if (criteria.ownerId() != null) {
                predicates.add(cb.equal(root.get("ownerId"), criteria.ownerId()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    static Specification<ContactJpaEntity> contacts(ContactCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!criteria.includeArchived()) {
                predicates.add(cb.isFalse(root.get("archived")));
            }
            if (hasText(criteria.term())) {
                String like = "%" + criteria.term().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("jobTitle")), like)
                ));
            }
            if (criteria.companyId() != null) {
                predicates.add(cb.equal(root.get("companyId"), criteria.companyId()));
            }
            if (criteria.decisionRole() != null) {
                predicates.add(cb.equal(root.get("decisionRole"), criteria.decisionRole()));
            }
            if (criteria.active() != null) {
                predicates.add(cb.equal(root.get("active"), criteria.active()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
