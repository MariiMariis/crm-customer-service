package com.pb.crm.catalog.api;

import com.pb.crm.catalog.application.product.ProductService;
import com.pb.crm.catalog.application.product.dto.CategoryTaxonomyResponse;
import com.pb.crm.catalog.application.product.dto.ProductRequest;
import com.pb.crm.catalog.application.product.dto.ProductResponse;
import com.pb.crm.catalog.domain.product.BillingType;
import com.pb.crm.catalog.domain.product.ProductCategory;
import com.pb.crm.catalog.domain.product.ProductCriteria;
import com.pb.crm.catalog.domain.product.ProductSubcategory;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> findBySku(@PathVariable String sku) {
        return ResponseEntity.ok(service.findBySku(sku));
    }

    @GetMapping("/taxonomy")
    public ResponseEntity<List<CategoryTaxonomyResponse>> taxonomy() {
        return ResponseEntity.ok(CategoryTaxonomyResponse.all());
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> search(
            @RequestParam(name = "q", required = false) String term,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ProductSubcategory subcategory,
            @RequestParam(required = false) BillingType billing,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        ProductCriteria criteria = new ProductCriteria(term, category, subcategory, billing, active, includeArchived);
        return ResponseEntity.ok(PageResponse.from(service.search(criteria, new PageQuery(page, pageSize))));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ProductResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(service.activate(id));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ProductResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(service.deactivate(id));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ProductResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(service.archive(id));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ProductResponse> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<AuditRevision<ProductResponse>>> revisions(@PathVariable Long id) {
        return ResponseEntity.ok(service.findRevisions(id));
    }
}
