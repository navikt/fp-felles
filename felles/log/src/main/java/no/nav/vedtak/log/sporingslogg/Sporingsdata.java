package no.nav.vedtak.log.sporingslogg;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * DTO for data som utgjør et innslag i sporingsloggen.
 */
public class Sporingsdata {

    private Map<SporingsloggId, String> verdier = new TreeMap<>(Comparator.comparing(SporingsloggId::getSporingsloggKode));
    private String action;

    private Sporingsdata(String action, Map<SporingsloggId, String> verdier) {
        this.action = action;
        this.verdier.putAll(verdier);
    }

    private Sporingsdata(String action) {
        this.action = action;
    }

    public static Sporingsdata opprett(String action) {
        return new Sporingsdata(action);
    }

    public Sporingsdata kopi() {
        return new Sporingsdata(action, verdier);
    }

    public Sporingsdata leggTilId(SporingsloggId navn, Long verdi) {
        String verdiStr = (verdi != null ? verdi.toString() : "");
        return leggTilId(navn, verdiStr);
    }

    public Sporingsdata leggTilId(SporingsloggId navn, String verdi) {
        verdier.put(navn, verdi);
        return this;
    }

    public Set<SporingsloggId> getNøkler() {
        return verdier.keySet();
    }

    public String getVerdi(SporingsloggId navn) {
        return verdier.get(navn);
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
}
