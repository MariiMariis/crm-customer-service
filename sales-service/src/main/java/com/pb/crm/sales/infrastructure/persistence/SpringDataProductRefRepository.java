package com.pb.crm.sales.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductRefRepository extends JpaRepository<ProductRefJpaEntity, Long> {
}
