package com.pb.crm.customer;

import com.pb.crm.audit.RevisionResponse;
import com.pb.crm.common.PageResponse;
import com.pb.crm.customer.dto.CustomerRequest;
import com.pb.crm.customer.dto.CustomerResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(Long id, CustomerRequest request);

    CustomerResponse findById(Long id);

    List<CustomerResponse> findAll();

    PageResponse<CustomerResponse> search(String term, Pageable pageable);

    void delete(Long id);

    List<RevisionResponse<CustomerResponse>> findRevisions(Long id);
}
