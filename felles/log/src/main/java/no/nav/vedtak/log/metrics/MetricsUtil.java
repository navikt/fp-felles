package no.nav.vedtak.log.metrics;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.micrometer.prometheus.PrometheusConfig.DEFAULT;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class MetricsUtil {
    public static final PrometheusMeterRegistry REGISTRY = new PrometheusMeterRegistry(DEFAULT);

    static {
        Metrics.addRegistry(REGISTRY);
        new ClassLoaderMetrics().bindTo(globalRegistry);
        new JvmMemoryMetrics().bindTo(globalRegistry);
        new JvmGcMetrics().bindTo(globalRegistry);
        new ProcessorMetrics().bindTo(globalRegistry);
        new JvmThreadMetrics().bindTo(globalRegistry);
    }

    public static String scrape() {
        return REGISTRY.scrape();
    }
}
