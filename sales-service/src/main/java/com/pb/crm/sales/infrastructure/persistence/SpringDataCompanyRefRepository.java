package com.pb.crm.sales.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCompanyRefRepository extends JpaRepository<CompanyRefJpaEntity, Long> {
}
