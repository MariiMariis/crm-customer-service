package com.pb.crm.customer;

import com.pb.crm.common.ResourceNotFoundException;
import com.pb.crm.customer.dto.CustomerRequest;
import com.pb.crm.customer.dto.CustomerResponse;
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
        if (customerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("ja existe um cliente cadastrado com este email");
        }
        Customer customer = new Customer(request.name(), request.email(), request.phone(), request.document());
        Customer saved = customerRepository.save(customer);
        return CustomerResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findEntityById(id);
        customer.update(request.name(), request.email(), request.phone(), request.document());
        return CustomerResponse.fromEntity(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return CustomerResponse.fromEntity(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = findEntityById(id);
        customerRepository.delete(customer);
    }

    private Customer findEntityById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Cliente", id));
    }
}
