package com.pb.crm.commons.domain;

import com.pb.crm.commons.error.BusinessRuleException;

import java.time.Instant;

public abstract class AggregateRoot {

    private Long id;
    private AuditInfo audit = AuditInfo.empty();
    private boolean archived;
    private Instant archivedAt;

    protected AggregateRoot() {
    }

    protected void rehydrateBase(Long id, AuditInfo audit, boolean archived, Instant archivedAt) {
        this.id = id;
        this.audit = audit == null ? AuditInfo.empty() : audit;
        this.archived = archived;
        this.archivedAt = archivedAt;
    }

    public void archive() {
        if (archived) {
            throw new BusinessRuleException("o registro ja esta arquivado");
        }
        archived = true;
        archivedAt = Instant.now();
    }

    public void restore() {
        if (!archived) {
            throw new BusinessRuleException("o registro nao esta arquivado");
        }
        archived = false;
        archivedAt = null;
    }

    protected void assertNotArchived() {
        if (archived) {
            throw new BusinessRuleException("o registro esta arquivado e nao pode ser alterado");
        }
    }

    public boolean isNew() {
        return id == null;
    }

    public Long getId() {
        return id;
    }

    public AuditInfo getAudit() {
        return audit;
    }

    public Long getVersion() {
        return audit.version();
    }

    public boolean isArchived() {
        return archived;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }
}
