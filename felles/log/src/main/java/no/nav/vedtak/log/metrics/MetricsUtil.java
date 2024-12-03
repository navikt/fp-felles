package no.nav.vedtak.log.metrics;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.micrometer.prometheusmetrics.PrometheusConfig.DEFAULT;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

public class MetricsUtil {
    public static final PrometheusMeterRegistry REGISTRY = new PrometheusMeterRegistry(DEFAULT);

    private MetricsUtil() {

    }

    static {
        Metrics.addRegistry(REGISTRY);
        // JVM
        new ClassLoaderMetrics().bindTo(globalRegistry);
        new JvmMemoryMetrics().bindTo(globalRegistry);
        new JvmGcMetrics().bindTo(globalRegistry);
        new JvmThreadMetrics().bindTo(globalRegistry);
        new JvmInfoMetrics().bindTo(globalRegistry);
        // System
        new ProcessorMetrics().bindTo(globalRegistry);
        new UptimeMetrics().bindTo(globalRegistry);
        // Logging
        new LogbackMetrics().bindTo(globalRegistry);
    }

    public static String scrape() {
        return REGISTRY.scrape();
    }

    // Til info Eksempel på timer med percentiler
    // Timer.builder(navn).publishPercentiles(double... percentiles).publishPercentileHistogram(boolean).register(globalRegistry);

}
