package com.pb.crm.accounts.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.Optional;

public interface SpringDataCompanyRepository extends JpaRepository<CompanyJpaEntity, Long>,
        JpaSpecificationExecutor<CompanyJpaEntity>,
        RevisionRepository<CompanyJpaEntity, Long, Integer> {

    Optional<CompanyJpaEntity> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
