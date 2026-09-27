package com.pb.crm.sales.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "contact_refs")
public class ContactRefJpaEntity {

    @Id
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(length = 160)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean archived;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    protected ContactRefJpaEntity() {
    }

    public ContactRefJpaEntity(Long id) {
        this.id = id;
    }

    public void apply(Long companyId, String fullName, String email, boolean active, boolean archived) {
        this.companyId = companyId;
        this.fullName = fullName;
        this.email = email;
        this.active = active;
        this.archived = archived;
        this.syncedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getFullName() {
        return fullName;
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
}
