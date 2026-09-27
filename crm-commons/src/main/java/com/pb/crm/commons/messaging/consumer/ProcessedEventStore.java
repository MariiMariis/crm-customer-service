package com.pb.crm.commons.messaging.consumer;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public class ProcessedEventStore {

    private final JdbcTemplate jdbcTemplate;

    public ProcessedEventStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean markProcessed(String consumer, UUID eventId, String eventType) {
        int inserted = jdbcTemplate.update("""
                insert into processed_events (consumer, event_id, event_type, processed_at)
                values (?, ?, ?, now())
                on conflict (consumer, event_id) do nothing
                """, consumer, eventId, eventType);
        return inserted == 1;
    }

    public long countProcessed(String consumer) {
        Long total = jdbcTemplate.queryForObject(
                "select count(*) from processed_events where consumer = ?", Long.class, consumer);
        return total == null ? 0 : total;
    }
}
