package com.pb.crm.team.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.history.RevisionRepository;

public interface SpringDataSalesRepRepository extends JpaRepository<SalesRepJpaEntity, Long>,
        JpaSpecificationExecutor<SalesRepJpaEntity>,
        RevisionRepository<SalesRepJpaEntity, Long, Integer> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByManagerIdAndActiveTrueAndArchivedFalse(Long managerId);
}
