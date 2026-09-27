package com.pb.crm.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.Optional;

public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, Long>,
        JpaSpecificationExecutor<ProductJpaEntity>,
        RevisionRepository<ProductJpaEntity, Long, Integer> {

    Optional<ProductJpaEntity> findBySku(String sku);

    @Query(value = "select nextval('product_sku_seq')", nativeQuery = true)
    long nextSkuSequence();
}
