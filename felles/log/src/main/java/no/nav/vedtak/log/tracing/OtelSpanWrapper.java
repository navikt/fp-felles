package no.nav.vedtak.log.tracing;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

/**
 * Gir mulighet for å opprette opentelemetry traces og spans i biblioteker og applikasjoner.
 */
public class OtelSpanWrapper {
    private final Tracer tracer;

    public static OtelSpanWrapper forApplikasjon() {
        var tracer = GlobalOpenTelemetry.getTracer("application");
        return new OtelSpanWrapper(tracer);
    }

    public OtelSpanWrapper(Tracer tracer) {
        this.tracer = tracer;
    }

    public <V> V span(String navn,
                      UnaryOperator<SpanBuilder> spanBuilderTransformer,
                      Supplier<V> supplier) {
        var span = startSpan(navn, spanBuilderTransformer);
        try (var unused = span.makeCurrent()) {
            return supplier.get();
        } catch (RuntimeException e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
    }

    public <E extends Exception> void span(String navn,
                                           UnaryOperator<SpanBuilder> spanBuilderTransformer,
                                           RunnableWithException<E> runnable) throws E {
        var span = startSpan(navn, spanBuilderTransformer);
        try (var unused = span.makeCurrent()) {
            runnable.run();
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
    }

    public <V> V spanCallable(String navn,
                      UnaryOperator<SpanBuilder> spanBuilderTransformer,
                      Callable<V> runnable) throws Exception {
        var span = startSpan(navn, spanBuilderTransformer);
        try (var unused = span.makeCurrent()) {
            return runnable.call();
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
    }

    private Span startSpan(String navn, UnaryOperator<SpanBuilder> spanBuilderTransformer) {
        var spanBuilder = tracer.spanBuilder(navn);
        spanBuilder = spanBuilderTransformer.apply(spanBuilder);
        return spanBuilder.startSpan();
    }

    @FunctionalInterface
    public interface RunnableWithException<E extends Exception> {
        void run() throws E;
    }
}
