package com.pb.crm.accounts.domain.company;

import java.math.BigDecimal;

public record CompanyProfile(
        String legalName,
        String tradeName,
        Cnpj cnpj,
        Industry industry,
        CompanySize size,
        Integer employees,
        BigDecimal annualRevenue,
        String website,
        String phone,
        String city,
        BrazilianState state,
        CompanyType type,
        String notes
) {
}
