package com.pb.crm.sales.domain.reference;

public record ContactRef(
        Long id,
        Long companyId,
        String fullName,
        String email,
        boolean active,
        boolean archived
) {
    public boolean isAvailable() {
        return active && !archived;
    }
}
