package com.pb.crm.accounts.domain.salesrep;

public record SalesRepRef(
        Long id,
        String name,
        String email,
        boolean active,
        boolean archived
) {
    public boolean canOwnRecords() {
        return active && !archived;
    }
}
