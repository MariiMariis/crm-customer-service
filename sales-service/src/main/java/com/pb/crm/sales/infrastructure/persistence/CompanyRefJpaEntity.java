package com.pb.crm.sales.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "company_refs")
public class CompanyRefJpaEntity {

    @Id
    private Long id;

    @Column(name = "display_name", nullable = false, length = 160)
    private String displayName;

    @Column(length = 14)
    private String cnpj;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(nullable = false)
    private boolean archived;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    @Column(name = "last_event_at")
    private Instant lastEventAt;

    protected CompanyRefJpaEntity() {
    }

    public CompanyRefJpaEntity(Long id) {
        this.id = id;
    }

    public void apply(String displayName, String cnpj, Long ownerId, boolean archived, Instant occurredAt) {
        this.displayName = displayName;
        this.cnpj = cnpj;
        this.ownerId = ownerId;
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

    public String getDisplayName() {
        return displayName;
    }

    public String getCnpj() {
        return cnpj;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public boolean isArchived() {
        return archived;
    }
}
