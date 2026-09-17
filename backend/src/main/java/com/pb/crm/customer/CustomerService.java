package com.pb.crm.customer;

import com.pb.crm.customer.dto.CustomerRequest;
import com.pb.crm.customer.dto.CustomerResponse;

import java.util.List;


public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(Long id, CustomerRequest request);

    CustomerResponse findById(Long id);

    List<CustomerResponse> findAll();

    void delete(Long id);
}
