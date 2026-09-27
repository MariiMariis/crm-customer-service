package com.pb.crm.sales.application.opportunity.dto;

import com.pb.crm.sales.domain.opportunity.OpportunityStage;

import java.math.BigDecimal;
import java.util.List;

public record PipelineResponse(
        List<StageSummary> stages,
        long openCount,
        BigDecimal openAmount,
        BigDecimal weightedForecast,
        BigDecimal wonAmount,
        BigDecimal monthlyRecurringWon,
        long pendingDiscountApprovals
) {
    public record StageSummary(
            OpportunityStage stage,
            long count,
            BigDecimal amount,
            BigDecimal weightedAmount
    ) {
    }
}
