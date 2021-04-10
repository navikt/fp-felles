package no.nav.vedtak.log.sporingslogg;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * DTO for data som utgjør et innslag i sporingsloggen.
 */
@Deprecated(since = "3.1.x", forRemoval = true)
/* Utgår erstattes av auditlog */
public class Sporingsdata {

    private NavigableMap<String, String> verdier;
    private String action;

    private Sporingsdata(String action, Map<SporingsloggId, String> verdier) {
        this.action = action;
        this.verdier = new TreeMap<>();
        this.verdier.putAll(verdier.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().getSporingsloggKode(), e -> e.getValue())));
    }

    private Sporingsdata(String action, NavigableMap<String, String> verdier) {
        this.action = action;
        this.verdier = verdier;
    }

    private Sporingsdata(String action) {
        this.action = action;
        this.verdier = new TreeMap<>();
    }

    public static Sporingsdata opprett(String action) {
        return new Sporingsdata(action);
    }

    public Sporingsdata kopi() {
        return new Sporingsdata(action, new TreeMap<>(verdier));
    }

    public Sporingsdata leggTilId(SporingsloggId navn, Long verdi) {
        String verdiStr = (verdi != null ? verdi.toString() : "");
        return leggTilId(navn, verdiStr);
    }

    public Sporingsdata leggTilId(SporingsloggId navn, String verdi) {
        verdier.put(navn.getSporingsloggKode(), verdi);
        return this;
    }

    public Sporingsdata leggTilId(String navn, String verdi) {
        verdier.put(navn, verdi);
        return this;
    }

    public Set<String> keySet(){
        return verdier.keySet();
    }

    public String getVerdi(SporingsloggId navn) {
        return verdier.get(navn.getSporingsloggKode());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Sporingsdata that = (Sporingsdata) o;
        return Objects.equals(verdier, that.verdier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verdier);
    }

    @Override
    public String toString() {
        return "Sporingsdata{" +
            ", verdier=" + verdier +
            '}';
    }

    public String getAction() {
        return action;
    }

    public Set<Entry<String, String>> entrySet() {
        return verdier.entrySet();
    }
}
