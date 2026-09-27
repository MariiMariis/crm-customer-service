package com.pb.crm.sales.domain.reference;

public record SalesRepRef(
        Long id,
        String name,
        String email,
        Long managerId,
        boolean active,
        boolean archived
) {
    public boolean canOwnRecords() {
        return active && !archived;
    }
}
