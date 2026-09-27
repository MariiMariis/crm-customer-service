package com.pb.crm.sales.domain.reference;

public record CompanyRef(
        Long id,
        String displayName,
        String cnpj,
        Long ownerId,
        boolean archived
) {
    public boolean acceptsOpportunities() {
        return !archived;
    }
}
