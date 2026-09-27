package com.pb.crm.sales.domain.opportunity;

import java.time.LocalDate;

public record OpportunityCriteria(
        String term,
        OpportunityStage stage,
        Boolean open,
        Long companyId,
        Long ownerId,
        DiscountApprovalStatus discountApproval,
        LocalDate closingFrom,
        LocalDate closingTo,
        boolean includeArchived
) {
}
