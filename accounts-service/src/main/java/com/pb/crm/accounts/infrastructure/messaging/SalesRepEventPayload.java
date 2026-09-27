package com.pb.crm.accounts.infrastructure.messaging;

public record SalesRepEventPayload(
        Long id,
        String name,
        String email,
        boolean active,
        boolean archived
) {
}
