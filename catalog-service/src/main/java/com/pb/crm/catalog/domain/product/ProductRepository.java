package com.pb.crm.catalog.domain.product;

import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    Optional<Product> findBySku(Sku sku);

    PageResult<Product> search(ProductCriteria criteria, PageQuery page);

    long nextSkuSequence();

    List<AuditRevision<Product>> findRevisions(Long id);
}
