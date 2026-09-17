package com.pb.crm.customer;

import com.pb.crm.audit.RevisionResponse;
import com.pb.crm.common.BusinessRuleException;
import com.pb.crm.common.PageResponse;
import com.pb.crm.common.ResourceNotFoundException;
import com.pb.crm.customer.dto.CustomerRequest;
import com.pb.crm.customer.dto.CustomerResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new BusinessRuleException("ja existe um cliente cadastrado com este email");
        }
        String document = normalizeDocument(request.document());
        if (document != null && customerRepository.existsByDocument(document)) {
            throw new BusinessRuleException("ja existe um cliente cadastrado com este documento");
        }
        Customer customer = new Customer(request.name(), request.email(), request.phone(), request.document());
        return CustomerResponse.fromEntity(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findEntityById(id);
        assertVersionMatches(customer, request.version());
        if (customerRepository.existsByEmailIgnoreCaseAndIdNot(request.email().trim(), id)) {
            throw new BusinessRuleException("ja existe outro cliente cadastrado com este email");
        }
        String document = normalizeDocument(request.document());
        if (document != null && customerRepository.existsByDocumentAndIdNot(document, id)) {
            throw new BusinessRuleException("ja existe outro cliente cadastrado com este documento");
        }
        customer.update(request.name(), request.email(), request.phone(), request.document());
        return CustomerResponse.fromEntity(customerRepository.saveAndFlush(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return CustomerResponse.fromEntity(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAllByOrderByNameAsc().stream()
                .map(CustomerResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> search(String term, Pageable pageable) {
        String normalized = term == null ? "" : term.trim();
        return PageResponse.from(customerRepository.search(normalized, pageable), CustomerResponse::fromEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = findEntityById(id);
        customerRepository.delete(customer);
        customerRepository.flush();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevisionResponse<CustomerResponse>> findRevisions(Long id) {
        return customerRepository.findRevisions(id).stream()
                .map(revision -> RevisionResponse.from(revision, CustomerResponse::fromEntity))
                .toList();
    }

    private Customer findEntityById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Cliente", id));
    }

    private static void assertVersionMatches(Customer customer, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(customer.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Customer.class, customer.getId());
        }
    }

    private static String normalizeDocument(String document) {
        return document == null || document.isBlank() ? null : document.trim();
    }
}
