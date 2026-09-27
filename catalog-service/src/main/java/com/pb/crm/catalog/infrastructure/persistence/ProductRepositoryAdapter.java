package com.pb.crm.catalog.infrastructure.persistence;

import com.pb.crm.catalog.domain.product.Product;
import com.pb.crm.catalog.domain.product.ProductCriteria;
import com.pb.crm.catalog.domain.product.ProductRepository;
import com.pb.crm.catalog.domain.product.Sku;
import com.pb.crm.commons.audit.AuditRevisions;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductRepository jpaRepository;
    private final ProductMapper mapper;

    public ProductRepositoryAdapter(SpringDataProductRepository jpaRepository, ProductMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = product.isNew()
                ? new ProductJpaEntity()
                : jpaRepository.findById(product.getId())
                        .orElseThrow(() -> ResourceNotFoundException.forId("Produto", product.getId()));
        mapper.copyToEntity(product, entity);
        return mapper.toDomain(jpaRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findBySku(Sku sku) {
        return jpaRepository.findBySku(sku.value()).map(mapper::toDomain);
    }

    @Override
    public PageResult<Product> search(ProductCriteria criteria, PageQuery page) {
        Sort sort = Sort.by("category").ascending().and(Sort.by("name")).and(Sort.by("id"));
        Page<ProductJpaEntity> result = jpaRepository.findAll(
                ProductSpecifications.matching(criteria), PageRequest.of(page.page(), page.size(), sort));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    @Override
    public long nextSkuSequence() {
        return jpaRepository.nextSkuSequence();
    }

    @Override
    public List<AuditRevision<Product>> findRevisions(Long id) {
        return jpaRepository.findRevisions(id).stream()
                .map(revision -> AuditRevisions.from(revision, mapper::toDomain))
                .toList();
    }
}
