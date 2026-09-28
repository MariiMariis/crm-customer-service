package com.pb.crm.commons.messaging.outbox;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxTracingTest {

    @RegisterExtension
    static final OpenTelemetryExtension otel = OpenTelemetryExtension.create();

    private Tracer tracer() {
        return otel.getOpenTelemetry().getTracer("teste");
    }

    @Test
    void deveCapturarOTraceparentDaRequisicaoQueGerouOEvento() {
        Span request = tracer().spanBuilder("POST /api/leads").startSpan();
        try (Scope ignored = request.makeCurrent()) {
            String traceparent = OutboxTracing.currentTraceparent();

            assertThat(traceparent).isEqualTo("00-%s-%s-01".formatted(
                    request.getSpanContext().getTraceId(),
                    request.getSpanContext().getSpanId()));
        } finally {
            request.end();
        }
    }

    @Test
    void naoDeveGravarTraceparentSemSpanAtivo() {
        assertThat(OutboxTracing.currentTraceparent()).isNull();
    }

    @Test
    void relayDeveContinuarOTraceGravadoNoOutbox() {
        String traceId = "4bf92f3577b34da6a3ce929d0e0e4736";
        String spanId = "00f067aa0ba902b7";
        OutboxEvent event = evento("00-%s-%s-01".formatted(traceId, spanId));

        OutboxTracing.startPublishSpan(event).end();

        SpanData span = otel.getSpans().getFirst();
        assertThat(span.getName()).isEqualTo("outbox relay sales.lead.created");
        assertThat(span.getTraceId()).isEqualTo(traceId);
        assertThat(span.getParentSpanId()).isEqualTo(spanId);
        assertThat(span.getAttributes().asMap().values()).contains("sales.events", "sales.lead.created");
    }

    @Test
    void relayDeveIniciarNovoTraceQuandoOEventoNaoTemOrigem() {
        OutboxTracing.startPublishSpan(evento(null)).end();

        SpanData span = otel.getSpans().getFirst();
        assertThat(span.getParentSpanContext().isValid()).isFalse();
    }

    @Test
    void pollingDoRelayNaoDeveGerarTraces() {
        try (Scope ignored = OutboxTracing.untracedPolling()) {
            Span query = tracer().spanBuilder("SELECT outbox_events").startSpan();
            assertThat(query.getSpanContext().isSampled()).isFalse();
            query.end();
        }

        assertThat(otel.getSpans()).isEmpty();
    }

    private static OutboxEvent evento(String traceparent) {
        return new OutboxEvent(
                UUID.randomUUID(),
                "sales.events",
                "sales.lead.created",
                "sales.lead.created",
                "Lead",
                "42",
                "{}",
                Instant.now(),
                traceparent);
    }
}
