package com.pb.crm.sales.domain.opportunity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OpportunityDetails(
        String title,
        String description,
        LocalDate expectedCloseDate,
        int contractTermMonths,
        BigDecimal estimatedValue
) {
}
