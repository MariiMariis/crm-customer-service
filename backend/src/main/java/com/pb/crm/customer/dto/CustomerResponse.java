package com.pb.crm.customer.dto;

import com.pb.crm.customer.Customer;

import java.time.Instant;

/**
 * Payload de saida com os dados do cliente.
 */
public record CustomerResponse(
        Long id,
        String name,
        String email,
        String phone,
        String document,
        Instant createdAt
) {
    public static CustomerResponse fromEntity(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDocument(),
                customer.getCreatedAt()
        );
    }
}
