package com.pb.crm.commons.domain;

import java.time.Instant;

public record AuditInfo(
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        Long version
) {
    public static AuditInfo empty() {
        return new AuditInfo(null, null, null, null, null);
    }
}
