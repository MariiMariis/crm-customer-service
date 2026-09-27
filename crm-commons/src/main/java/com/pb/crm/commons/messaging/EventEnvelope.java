package com.pb.crm.commons.messaging;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String source,
        String aggregateType,
        String aggregateId,
        String correlationId,
        String causationId,
        String actor,
        JsonNode payload
) {
    public static final int CURRENT_VERSION = 1;
}
