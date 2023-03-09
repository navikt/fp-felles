package no.nav.vedtak.log.metrics;

import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.micrometer.prometheus.PrometheusConfig.DEFAULT;

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
    }

    public static String scrape() {
        return REGISTRY.scrape();
    }


    public static void utvidMedMedian(String navn) {
        utvidMedPercentiler(navn, 0.5);
    }

    public static void utvidMedPercentiler(String navn, double... percentiles) {
        globalRegistry.config().meterFilter(new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Id id, DistributionStatisticConfig config) {
                if (id.getName().equals(navn)) {
                    return DistributionStatisticConfig.builder()
                        .percentilesHistogram(false)
                        .percentiles(percentiles)
                        .build()
                        .merge(config);
                }
                return config;
            }
        });
    }

    public static void utvidMedHistogram(String navn) {
        utvidMedHistogram(navn, 0.5, 0.95, 0.99);
    }

    public static void utvidMedHistogram(String navn, double... percentiles) {
        globalRegistry.config().meterFilter(new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Id id, DistributionStatisticConfig config) {
                if (id.getName().equals(navn)) {
                    return DistributionStatisticConfig.builder()
                        .percentilesHistogram(true)
                        .percentiles(percentiles)
                        .build()
                        .merge(config);
                }
                return config;
            }
        });
    }
}
