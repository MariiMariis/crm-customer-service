package com.pb.crm.sales.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.history.RevisionRepository;

public interface SpringDataActivityRepository extends JpaRepository<ActivityJpaEntity, Long>,
        JpaSpecificationExecutor<ActivityJpaEntity>,
        RevisionRepository<ActivityJpaEntity, Long, Integer> {
}
