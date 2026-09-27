package com.pb.crm.sales.application.lead.dto;

import com.pb.crm.sales.domain.lead.LeadStatus;

import java.util.Map;

public record LeadStatsResponse(
        long total,
        long open,
        Map<LeadStatus, Long> byStatus
) {
}
