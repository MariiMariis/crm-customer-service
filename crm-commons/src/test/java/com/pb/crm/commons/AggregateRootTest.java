package com.pb.crm.commons;

import com.pb.crm.commons.domain.AggregateRoot;
import com.pb.crm.commons.domain.PageQuery;
import com.pb.crm.commons.domain.PageResult;
import com.pb.crm.commons.error.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AggregateRootTest {

    private static final class SampleAggregate extends AggregateRoot {

        void change() {
            assertNotArchived();
        }
    }

    @Test
    void archiveAndRestoreToggleTheStateAndTimestamp() {
        SampleAggregate aggregate = new SampleAggregate();

        aggregate.archive();
        assertThat(aggregate.isArchived()).isTrue();
        assertThat(aggregate.getArchivedAt()).isNotNull();

        aggregate.restore();
        assertThat(aggregate.isArchived()).isFalse();
        assertThat(aggregate.getArchivedAt()).isNull();
    }

    @Test
    void archivedAggregateRejectsChangesAndSecondArchive() {
        SampleAggregate aggregate = new SampleAggregate();
        aggregate.archive();

        assertThatThrownBy(aggregate::change).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(aggregate::archive).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void restoringActiveAggregateIsRejected() {
        assertThatThrownBy(new SampleAggregate()::restore).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void newAggregateHasNoIdentityNorVersion() {
        SampleAggregate aggregate = new SampleAggregate();

        assertThat(aggregate.isNew()).isTrue();
        assertThat(aggregate.getVersion()).isNull();
    }

    @Test
    void pageQueryRejectsInvalidBoundaries() {
        assertThatThrownBy(() -> new PageQuery(-1, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PageQuery(0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PageQuery(0, PageQuery.MAX_SIZE + 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pageResultComputesTotalPagesAndMapsContent() {
        PageResult<Integer> result = new PageResult<>(List.of(1, 2), 0, 2, 5);

        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.map(String::valueOf).content()).containsExactly("1", "2");
    }
}
