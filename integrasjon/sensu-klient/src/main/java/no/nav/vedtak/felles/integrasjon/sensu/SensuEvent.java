package no.nav.vedtak.felles.integrasjon.sensu;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.util.env.Cluster;
import no.nav.vedtak.util.env.Environment;
import no.nav.vedtak.util.env.Namespace;

public class SensuEvent {
    private final String metricName;
    private final Map<String, String> tags;
    private final Map<String, Object> fields;

    private static final Map<String, String> defaultTags = Map.of(
            "application", getAppName(),
            "cluster", Cluster.current().clusterName(),
            "namespace", Namespace.current().getNamespace()
    );

    private SensuEvent(String metricName, Map<String, String> tags, Map<String, Object> fields) {
        this.metricName = Objects.requireNonNull(metricName, "Metrikk navn må ikke være null.");
        this.fields = Objects.requireNonNull(fields, "Metrikk felter må ikke være null.");
        if (fields.isEmpty()) {
            throw new IllegalStateException("Det må være minst enn metrikk i felter.");
        }
        this.tags = (null != tags ? tags : Map.of());
    }

    /**
     * Representerer en objekt som kan bli prosesert av sensu og influx.
     * @param metricName - navn til metrikk i influx, blir automatisk prefixet med app navn fra miljø.
     * @param tags - ekstra taggene som representerer kolonner i influx.
     *             Skal vanligvis representere String konstanter som ikke varierer alt for mye.
     *             Brukes til group by i influx.
     *             Standard taggene som application, cluster og namespace er automatisk lagd til.
     * @param fields - felter som representeret målinger i influx.
     *               Skal vannligvis representere tall.
     * @return SensuEvent
     */
    public static SensuEvent createSensuEvent(String metricName, Map<String, String> tags, Map<String, Object> fields) {
        return new SensuEvent(metricName, tags, fields);
    }

    /**
     * Representerer en objekt som kan bli prosesert av sensu og influx.
     * @param metricName - navn til metrikk i influx, blir automatisk prefixet med app navn fra miljø.
     * @param fields - felter som representeret målinger i influx. Skal vannligvis representere tall.
     * @return SensuEvent
     */
    public static SensuEvent createSensuEvent(String metricName, Map<String, Object> fields) {
        return new SensuEvent(metricName, Map.of(), fields);
    }

    SensuRequest toSensuRequest() {
        return new SensuRequest(toSensuEventName(), toPoint().lineProtocol());
    }

    private String toSensuEventName() {
        return "sensu-event-" + getAppName();
    }

    private static String getAppName() {
        return Environment.current().getProperty("NAIS_APP_NAME", "local-app");
    }

    private Point toPoint() {
        return Point.measurement(getAppName() + "." + metricName)
                .time(TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()), TimeUnit.NANOSECONDS)
                .tag(defaultTags)
                .tag(tags)
                .fields(fields)
                .build();
    }

    static class SensuRequest {
        private static final ObjectMapper OM = new ObjectMapper();
        private final String name;
        private final String type;
        private final List<String> handlers;
        private final int status = 0;
        private final String output;

        public SensuRequest(String name, String output) {
            this.name = name;
            this.output = output;
            this.type = "metric";
            this.handlers = List.of("events_nano");
        }

        public String toJson() throws JsonProcessingException {
            return OM.writeValueAsString(this);
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public List<String> getHandlers() {
            return handlers;
        }

        public String getOutput() {
            return output;
        }
        
        public int getStatus() {
            return status;
        }
    }
}


