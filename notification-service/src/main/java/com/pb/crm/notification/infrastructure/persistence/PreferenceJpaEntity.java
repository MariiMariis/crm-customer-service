package com.pb.crm.notification.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notification_preferences")
public class PreferenceJpaEntity {

    @Id
    @Column(name = "sales_rep_id")
    private Long salesRepId;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PreferenceJpaEntity() {
    }

    public PreferenceJpaEntity(Long salesRepId) {
        this.salesRepId = salesRepId;
    }

    public void apply(boolean emailEnabled, Instant updatedAt) {
        this.emailEnabled = emailEnabled;
        this.updatedAt = updatedAt;
    }

    public Long getSalesRepId() {
        return salesRepId;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
