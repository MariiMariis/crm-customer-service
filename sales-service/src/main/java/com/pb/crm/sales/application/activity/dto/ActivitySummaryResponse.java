package com.pb.crm.sales.application.activity.dto;

public record ActivitySummaryResponse(
        long overdue,
        long dueToday,
        long dueNext7Days,
        long doneLast7Days
) {
}
