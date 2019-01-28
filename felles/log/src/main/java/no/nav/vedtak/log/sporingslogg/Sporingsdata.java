package no.nav.vedtak.log.sporingslogg;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DTO for data som utgjør et innslag i sporingsloggen.
 */
public class Sporingsdata {

    private Map<SporingsloggId, String> verdier = new EnumMap<>(SporingsloggId.class);

    private Sporingsdata() {

    }

    private Sporingsdata(Map<SporingsloggId, String> verdier) {
        this.verdier.putAll(verdier);
    }

    public static Sporingsdata opprett() {
        return new Sporingsdata();
    }

    public Sporingsdata kopi(){
        return new Sporingsdata(verdier);
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
}
