package com.pb.crm.accounts.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.history.RevisionRepository;

public interface SpringDataCompanyRepository extends JpaRepository<CompanyJpaEntity, Long>,
        JpaSpecificationExecutor<CompanyJpaEntity>,
        RevisionRepository<CompanyJpaEntity, Long, Integer> {

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
