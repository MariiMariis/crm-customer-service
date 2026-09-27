package com.pb.crm.sales.domain.opportunity;

import java.time.Instant;

public record StageChange(
        Long id,
        OpportunityStage fromStage,
        OpportunityStage toStage,
        int probability,
        String reason,
        String changedBy,
        Instant changedAt
) {
}
