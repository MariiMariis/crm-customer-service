package com.pb.crm.commons.audit;

import com.pb.crm.commons.domain.AuditRevision;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;

import java.util.function.Function;

public final class AuditRevisions {

    private static final String UNKNOWN_ACTOR = "unknown";

    private AuditRevisions() {
    }

    public static <E, T> AuditRevision<T> from(Revision<Integer, E> revision, Function<E, T> mapper) {
        RevisionMetadata<Integer> metadata = revision.getMetadata();
        String actor = metadata.getDelegate() instanceof CrmRevisionEntity delegate
                ? delegate.getActor()
                : UNKNOWN_ACTOR;
        return new AuditRevision<>(
                metadata.getRequiredRevisionNumber(),
                metadata.getRequiredRevisionInstant(),
                metadata.getRevisionType().name(),
                actor,
                mapper.apply(revision.getEntity())
        );
    }
}
