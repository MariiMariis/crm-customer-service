package com.pb.crm.catalog.application.product;

import com.pb.crm.catalog.application.product.dto.ProductRequest;
import com.pb.crm.catalog.application.product.dto.ProductResponse;
import com.pb.crm.catalog.domain.product.Product;
import com.pb.crm.catalog.domain.product.ProductCriteria;
import com.pb.crm.catalog.domain.product.ProductDetails;
import com.pb.crm.catalog.domain.product.ProductRepository;
import com.pb.crm.catalog.domain.product.Sku;
import com.pb.crm.commons.domain.AuditRevision;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.ResourceNotFoundException;
import com.pb.crm.commons.messaging.DomainEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private static final String EVENT_PREFIX = "catalog.";
    private static final String AGGREGATE_TYPE = "Product";

    private final ProductRepository repository;
    private final DomainEventPublisher eventPublisher;

    public ProductServiceImpl(ProductRepository repository, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        ProductDetails details = toDetails(request);
        Sku sku = Sku.generate(request.subcategory(), repository.nextSkuSequence());
        return ProductResponse.from(persist(Product.register(sku, details)));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = load(id);
        if (request.version() != null && !request.version().equals(product.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Product.class, id);
        }
        product.update(toDetails(request));
        return ProductResponse.from(persist(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(load(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findBySku(String sku) {
        Sku parsed = new Sku(sku.trim().toUpperCase());
        return repository.findBySku(parsed)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado para o SKU: " + parsed));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ProductResponse> search(ProductCriteria criteria, PageQuery page) {
        return repository.search(criteria, page).map(ProductResponse::from);
    }

    @Override
    @Transactional
    public ProductResponse activate(Long id) {
        Product product = load(id);
        product.activate();
        return ProductResponse.from(persist(product));
    }

    @Override
    @Transactional
    public ProductResponse deactivate(Long id) {
        Product product = load(id);
        product.deactivate();
        return ProductResponse.from(persist(product));
    }

    @Override
    @Transactional
    public ProductResponse archive(Long id) {
        Product product = load(id);
        product.archive();
        return ProductResponse.from(persist(product));
    }

    @Override
    @Transactional
    public ProductResponse restore(Long id) {
        Product product = load(id);
        product.restore();
        return ProductResponse.from(persist(product));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRevision<ProductResponse>> findRevisions(Long id) {
        load(id);
        return repository.findRevisions(id).stream()
                .map(revision -> revision.map(ProductResponse::from))
                .toList();
    }

    private Product persist(Product product) {
        List<String> events = product.pullEvents();
        Product saved = repository.save(product);
        ProductSnapshot snapshot = ProductSnapshot.from(saved);
        events.forEach(event -> eventPublisher.publish(EVENT_PREFIX + event, AGGREGATE_TYPE, saved.getId(), snapshot));
        return saved;
    }

    private Product load(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Produto", id));
    }

    private static ProductDetails toDetails(ProductRequest request) {
        return new ProductDetails(
                request.name(),
                request.description(),
                request.subcategory(),
                request.billing(),
                request.unit(),
                request.unitPrice(),
                request.unitCost(),
                request.maxDiscountPercent(),
                request.manufacturer(),
                request.warrantyMonths()
        );
    }
}
