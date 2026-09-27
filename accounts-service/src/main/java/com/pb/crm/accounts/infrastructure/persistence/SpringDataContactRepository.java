package com.pb.crm.accounts.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataContactRepository extends JpaRepository<ContactJpaEntity, Long>,
        JpaSpecificationExecutor<ContactJpaEntity>,
        RevisionRepository<ContactJpaEntity, Long, Integer> {

    List<ContactJpaEntity> findByCompanyIdAndArchivedFalseOrderByFirstNameAscLastNameAsc(Long companyId);

    Optional<ContactJpaEntity> findFirstByCompanyIdAndPrimaryTrueAndArchivedFalse(Long companyId);

    long countByCompanyIdAndArchivedFalse(Long companyId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
