package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class Orgnummer {

    private final String orgnr;

    @JsonCreator
    public static Orgnummer forValue(String orgnr) {
        return new Orgnummer(orgnr);
    }

    private Orgnummer(String orgnr) {
        this.orgnr = orgnr;
    }

    @JsonValue
    public String getOrgnr() {
        return orgnr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgnr);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof Orgnummer that && Objects.equals(this.orgnr, that.orgnr);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[orgnr=" + orgnr + "]";
    }
}
