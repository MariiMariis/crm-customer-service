package com.pb.crm.sales.domain.lead;

import java.math.BigDecimal;

public record LeadDetails(
        String firstName,
        String lastName,
        String email,
        String phone,
        String companyName,
        String jobTitle,
        LeadSource source,
        BigDecimal estimatedValue,
        String notes
) {
}
