package com.pb.crm.commons.audit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.envers.Audited;

import java.time.Instant;

@Audited
@MappedSuperclass
public abstract class ArchivableEntity extends AuditableEntity {

    @Column(nullable = false)
    private boolean archived;

    @Column(name = "archived_at")
    private Instant archivedAt;

    public void applyArchiveState(boolean archived, Instant archivedAt) {
        this.archived = archived;
        this.archivedAt = archivedAt;
    }

    public boolean isArchived() {
        return archived;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }
}
