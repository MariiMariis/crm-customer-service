package com.pb.crm.ticket.dto;

import com.pb.crm.ticket.TicketStatus;

import java.util.Map;

public record TicketStatsResponse(
        long total,
        Map<TicketStatus, Long> byStatus
) {
}
