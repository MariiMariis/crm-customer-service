package com.pb.crm.commons.domain;

import java.time.Instant;
import java.util.function.Function;

public record AuditRevision<T>(
        int revision,
        Instant timestamp,
        String type,
        String actor,
        T data
) {
    public <R> AuditRevision<R> map(Function<T, R> mapper) {
        return new AuditRevision<>(revision, timestamp, type, actor, mapper.apply(data));
    }
}
