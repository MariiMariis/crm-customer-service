package com.pb.crm.accounts.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "sales_rep_refs")
public class SalesRepRefJpaEntity {

    @Id
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 160)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean archived;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    @Column(name = "last_event_at")
    private Instant lastEventAt;

    protected SalesRepRefJpaEntity() {
    }

    public SalesRepRefJpaEntity(Long id) {
        this.id = id;
    }

    public void apply(String name, String email, boolean active, boolean archived, Instant occurredAt) {
        this.name = name;
        this.email = email;
        this.active = active;
        this.archived = archived;
        this.syncedAt = Instant.now();
        this.lastEventAt = occurredAt;
    }

    public boolean isStaleComparedTo(Instant occurredAt) {
        return lastEventAt != null && occurredAt != null && !occurredAt.isAfter(lastEventAt);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isArchived() {
        return archived;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }
}
