package com.pb.crm.commons.messaging.outbox;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

final class OutboxTracing {

    static final String TRACEPARENT = "traceparent";

    private static final String INSTRUMENTATION = "com.pb.crm.commons.outbox";

    private static final TextMapGetter<Map<String, String>> GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
            return carrier == null ? Set.of() : carrier.keySet();
        }

        @Override
        public String get(Map<String, String> carrier, String key) {
            return carrier == null ? null : carrier.get(key);
        }
    };

    private OutboxTracing() {
    }

    static String currentTraceparent() {
        if (!Span.current().getSpanContext().isValid()) {
            return null;
        }
        Map<String, String> carrier = new HashMap<>();
        W3CTraceContextPropagator.getInstance().inject(Context.current(), carrier, Map::put);
        return carrier.get(TRACEPARENT);
    }

    static Scope untracedPolling() {
        SpanContext unsampled = SpanContext.create(
                randomHex(32),
                randomHex(16),
                TraceFlags.getDefault(),
                TraceState.getDefault());
        return Context.root().with(Span.wrap(unsampled)).makeCurrent();
    }

    static Span startPublishSpan(OutboxEvent event) {
        Context parent = event.getTraceparent() == null
                ? Context.root()
                : W3CTraceContextPropagator.getInstance()
                        .extract(Context.root(), Map.of(TRACEPARENT, event.getTraceparent()), GETTER);
        return tracer().spanBuilder("outbox relay " + event.getEventType())
                .setParent(parent)
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute("messaging.system", "rabbitmq")
                .setAttribute("messaging.destination.name", event.getExchange())
                .setAttribute("messaging.rabbitmq.destination.routing_key", event.getRoutingKey())
                .setAttribute("messaging.message.id", event.getId().toString())
                .setAttribute("crm.aggregate.type", event.getAggregateType())
                .setAttribute("crm.aggregate.id", event.getAggregateId())
                .startSpan();
    }

    static void recordFailure(Span span, Exception ex) {
        span.recordException(ex);
        span.setStatus(StatusCode.ERROR, ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
    }

    private static Tracer tracer() {
        return GlobalOpenTelemetry.getTracer(INSTRUMENTATION);
    }

    private static String randomHex(int length) {
        StringBuilder builder = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (builder.length() < length) {
            builder.append(Long.toHexString(random.nextLong() | Long.MIN_VALUE));
        }
        return builder.substring(0, length);
    }
}
