package com.pb.crm.commons.messaging.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    public enum Status {
        PENDING,
        PUBLISHED
    }

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String exchange;

    @Column(name = "routing_key", nullable = false, length = 160)
    private String routingKey;

    @Column(name = "event_type", nullable = false, length = 160)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 80)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 80)
    private String aggregateId;

    @Column(nullable = false, columnDefinition = "text")
    private String envelope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxEvent() {
    }

    public OutboxEvent(UUID id,
                       String exchange,
                       String routingKey,
                       String eventType,
                       String aggregateType,
                       String aggregateId,
                       String envelope,
                       Instant createdAt) {
        this.id = id;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.envelope = envelope;
        this.createdAt = createdAt;
        this.status = Status.PENDING;
    }

    public void markPublished() {
        this.status = Status.PUBLISHED;
        this.publishedAt = Instant.now();
        this.attempts++;
        this.lastError = null;
    }

    public void markFailedAttempt(String error) {
        this.attempts++;
        this.lastError = error == null ? null : error.substring(0, Math.min(error.length(), 1000));
    }

    public UUID getId() {
        return id;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEnvelope() {
        return envelope;
    }

    public Status getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
