package com.pb.crm.commons.audit;

import com.pb.crm.commons.error.BusinessRuleException;
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

    public void archive() {
        if (archived) {
            throw new BusinessRuleException("o registro ja esta arquivado");
        }
        archived = true;
        archivedAt = Instant.now();
        touch();
    }

    public void restore() {
        if (!archived) {
            throw new BusinessRuleException("o registro nao esta arquivado");
        }
        archived = false;
        archivedAt = null;
        touch();
    }

    protected void assertNotArchived() {
        if (archived) {
            throw new BusinessRuleException("o registro esta arquivado e nao pode ser alterado");
        }
    }

    public boolean isArchived() {
        return archived;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }
}
