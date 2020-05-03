package no.nav.vedtak.felles.integrasjon.sensu;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.influxdb.dto.Point;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.util.env.Cluster;
import no.nav.vedtak.util.env.Environment;
import no.nav.vedtak.util.env.Namespace;

public class SensuEvent {

    private static final String APP_NAME = Environment.current().getProperty("NAIS_APP_NAME", "local-app");
    private static final String DEFAULT_SENSU_EVENT_NAME = "sensu-event-" + APP_NAME;

    private static final Map<String, String> DEFAULT_TAGS = Map.of(
        "application", getAppName(),
        "cluster", Cluster.current().clusterName(),
        "namespace", Namespace.current().getNamespace());

    private final String metricName;
    private final Map<String, String> tags;
    private final Map<String, Object> fields;
    private final long tidsstempel;

    private SensuEvent(String metricName, Map<String, String> tags, Map<String, Object> fields, long tidsstempel) {
        this.metricName = Objects.requireNonNull(metricName, "Metrikk navn må ikke være null.");
        this.fields = Objects.requireNonNull(fields, "Metrikk felter må ikke være null.");
        if (fields.isEmpty()) {
            throw new IllegalStateException("Det må være minst enn metrikk i felter.");
        }
        this.tidsstempel = tidsstempel;
        this.tags = (null != tags ? tags : Map.of());
    }

    /**
     * Representerer en objekt som kan bli prosesert av sensu og influx.
     * 
     * @param metricName - navn til metrikk i influx, blir automatisk prefixet med app navn fra miljø.
     * @param tags - ekstra taggene som representerer kolonner i influx.
     *            Skal vanligvis representere String konstanter som ikke varierer alt for mye.
     *            Brukes til group by i influx.
     *            Standard taggene som application, cluster og namespace er automatisk lagd til.
     * @param fields - felter som representeret målinger i influx.
     *            Skal vannligvis representere tall.
     * @param tidsstempel - millis (unit time) for når hendelsen inntraff
     * @return SensuEvent
     */
    public static SensuEvent createSensuEvent(String metricName, Map<String, String> tags, Map<String, Object> fields, long tidsstempel) {
        return new SensuEvent(metricName, tags, fields, tidsstempel);
    }

    /**
     * @see #createSensuEvent(String, Map, Map, long)
     */
    public static SensuEvent createSensuEvent(String metricName, Map<String, String> tags, Map<String, Object> fields) {
        return new SensuEvent(metricName, tags, fields, System.currentTimeMillis());
    }

    /**
     * @see #createSensuEvent(String, Map, Map, long)
     */
    public static SensuEvent createSensuEvent(String metricName, Map<String, Object> fields) {
        return new SensuEvent(metricName, Map.of(), fields, System.currentTimeMillis());
    }

    SensuRequest toSensuRequest() {
        return new SensuRequest(getSensuEventName(), toPoint().lineProtocol());
    }

    String getSensuEventName() {
        return DEFAULT_SENSU_EVENT_NAME;
    }

    private static String getAppName() {
        return APP_NAME;
    }

    private Point toPoint() {
        return Point.measurement(getAppName() + "." + metricName)
            .time(TimeUnit.MILLISECONDS.toNanos(tidsstempel), TimeUnit.NANOSECONDS)
            .tag(DEFAULT_TAGS)
            .tag(tags)
            .fields(fields)
            .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var other = (SensuEvent) obj;
        return Objects.equals(this.metricName, other.metricName)
            && Objects.equals(this.tags, other.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricName, tags);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<metricName=" + metricName +
            ", t=" + Instant.ofEpochMilli(tidsstempel) +
            ", tags=" + tags +
            ", fields=" + fields
            + ">";
    }

    static SensuRequest createBatchSensuRequest(List<SensuEvent> metrics) {
        return new SensuEvent.SensuRequest(metrics.stream().map(m -> m.toSensuRequest()).collect(Collectors.toList()));
    }

    static class SensuRequest {
        private static final ObjectMapper OM = new ObjectMapper();
        private final String name;
        private final String type;
        private final List<String> handlers;
        private final int status = 0;
        private final String output;

        /** Genrerer batch request fra list av requests. */
        SensuRequest(Collection<SensuRequest> list) {
            this(DEFAULT_SENSU_EVENT_NAME, list.stream().map(SensuRequest::getOutput).collect(Collectors.joining("\n")));
        }

        SensuRequest(String name, String output) {
            this.name = name;
            this.output = output;
            this.type = "metric";
            this.handlers = List.of("events_nano");
        }

        public String toJson() {
            try {
                return OM.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Ugyldig json: ", e);
            }
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
