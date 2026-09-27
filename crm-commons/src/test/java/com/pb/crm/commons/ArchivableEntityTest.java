package com.pb.crm.commons;

import com.pb.crm.commons.audit.ArchivableEntity;
import com.pb.crm.commons.error.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchivableEntityTest {

    private static final class SampleEntity extends ArchivableEntity {

        void change() {
            assertNotArchived();
        }
    }

    @Test
    void archiveAndRestoreToggleTheStateAndTimestamp() {
        SampleEntity entity = new SampleEntity();

        entity.archive();
        assertThat(entity.isArchived()).isTrue();
        assertThat(entity.getArchivedAt()).isNotNull();

        entity.restore();
        assertThat(entity.isArchived()).isFalse();
        assertThat(entity.getArchivedAt()).isNull();
    }

    @Test
    void archivedEntityRejectsChangesAndSecondArchive() {
        SampleEntity entity = new SampleEntity();
        entity.archive();

        assertThatThrownBy(entity::change).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(entity::archive).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void restoringActiveEntityIsRejected() {
        assertThatThrownBy(new SampleEntity()::restore).isInstanceOf(BusinessRuleException.class);
    }
}
