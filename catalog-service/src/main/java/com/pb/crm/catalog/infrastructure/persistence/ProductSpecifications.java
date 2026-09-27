package com.pb.crm.catalog.infrastructure.persistence;

import com.pb.crm.catalog.domain.product.ProductCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class ProductSpecifications {

    private ProductSpecifications() {
    }

    static Specification<ProductJpaEntity> matching(ProductCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!criteria.includeArchived()) {
                predicates.add(cb.isFalse(root.get("archived")));
            }
            if (criteria.term() != null && !criteria.term().isBlank()) {
                String like = "%" + criteria.term().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("sku")), like),
                        cb.like(cb.lower(root.get("manufacturer")), like)
                ));
            }
            if (criteria.category() != null) {
                predicates.add(cb.equal(root.get("category"), criteria.category()));
            }
            if (criteria.subcategory() != null) {
                predicates.add(cb.equal(root.get("subcategory"), criteria.subcategory()));
            }
            if (criteria.billing() != null) {
                predicates.add(cb.equal(root.get("billing"), criteria.billing()));
            }
            if (criteria.active() != null) {
                predicates.add(cb.equal(root.get("active"), criteria.active()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
