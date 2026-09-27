package com.pb.crm.catalog.application.product;

import com.pb.crm.catalog.application.product.dto.ProductRequest;
import com.pb.crm.catalog.application.product.dto.ProductResponse;
import com.pb.crm.catalog.domain.product.ProductCriteria;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    ProductResponse findById(Long id);

    ProductResponse findBySku(String sku);

    PageResult<ProductResponse> search(ProductCriteria criteria, PageQuery page);

    ProductResponse activate(Long id);

    ProductResponse deactivate(Long id);

    ProductResponse archive(Long id);

    ProductResponse restore(Long id);

    List<AuditRevision<ProductResponse>> findRevisions(Long id);
}
