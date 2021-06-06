package no.nav.vedtak.log.metrics;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.micrometer.prometheus.PrometheusConfig.DEFAULT;

import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
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
        new JvmInfoMetrics().bindTo(globalRegistry);
        new UptimeMetrics().bindTo(globalRegistry);
    }

    public static String scrape() {
        return REGISTRY.scrape();
    }

    public static void utvidMedHistogram(String navn) {
        globalRegistry.config().meterFilter(new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Id id, DistributionStatisticConfig config) {
                if (id.getName().equals(navn)) {
                    return DistributionStatisticConfig.builder()
                            .percentilesHistogram(true)
                            .percentiles(0.5, 0.95, 0.99).build().merge(config);
                }
                return config;
            }
        });
    }
}
