package com.pb.crm.notification.infrastructure.messaging;

import java.math.BigDecimal;

public final class SalesPayloads {

    private SalesPayloads() {
    }

    public record Lead(
            Long id,
            String fullName,
            String companyName,
            int score,
            Long ownerId,
            String ownerName
    ) {
    }

    public record Opportunity(
            Long id,
            String title,
            String companyName,
            Long ownerId,
            String ownerName,
            BigDecimal amount,
            String lossReason
    ) {
    }

    public record SalesRep(
            Long id,
            String name,
            String email,
            Long managerId,
            boolean active,
            boolean archived
    ) {
    }
}
