package no.nav.vedtak.log.metrics;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.micrometer.prometheusmetrics.PrometheusConfig.DEFAULT;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
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
        new ClassLoaderMetrics().bindTo(globalRegistry);
        new JvmMemoryMetrics().bindTo(globalRegistry);
        new JvmGcMetrics().bindTo(globalRegistry);
        new ProcessorMetrics().bindTo(globalRegistry);
        new JvmThreadMetrics().bindTo(globalRegistry);
        new JvmInfoMetrics().bindTo(globalRegistry);
        new UptimeMetrics().bindTo(globalRegistry);
        new LogbackMetrics().bindTo(globalRegistry);
    }

    public static String scrape() {
        return REGISTRY.scrape();
    }

    public static void timerUtenHistogram(String navn) {
        timerMedPercentiler(navn, 0.5, 0.95, 0.99);
    }

    public static void timerMedianUtenHistogram(String navn) {
        timerMedPercentiler(navn, 0.5);
    }

    public static void timerMedPercentiler(String navn, double... percentiles) {
        Timer.builder(navn)
            .publishPercentiles(percentiles)
            .publishPercentileHistogram(false)
            .register(globalRegistry);
    }

    public static void timerMedHistogram(String navn) {
        timerMedHistogram(navn, 0.5, 0.95, 0.99);
    }

    public static void timerMedianMedHistogram(String navn) {
        timerMedHistogram(navn, 0.5);
    }

    public static void timerMedHistogram(String navn, double... percentiles) {
        Timer.builder(navn)
            .publishPercentiles(percentiles)
            .publishPercentileHistogram(true)
            .register(globalRegistry);
    }

}
