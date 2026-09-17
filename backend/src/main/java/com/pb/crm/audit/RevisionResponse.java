package com.pb.crm.audit;

import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;

import java.time.Instant;
import java.util.function.Function;

public record RevisionResponse<T>(
        int revision,
        Instant timestamp,
        String type,
        String actor,
        T data
) {
    private static final String UNKNOWN_ACTOR = "unknown";

    public static <E, T> RevisionResponse<T> from(Revision<Integer, E> revision, Function<E, T> mapper) {
        RevisionMetadata<Integer> metadata = revision.getMetadata();
        String actor = metadata.getDelegate() instanceof CrmRevisionEntity delegate
                ? delegate.getActor()
                : UNKNOWN_ACTOR;
        return new RevisionResponse<>(
                metadata.getRequiredRevisionNumber(),
                metadata.getRequiredRevisionInstant(),
                metadata.getRevisionType().name(),
                actor,
                mapper.apply(revision.getEntity())
        );
    }
}
