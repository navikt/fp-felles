package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class Prosent {

    private final Integer prosent;

    @JsonCreator
    public static Prosent forValue(Integer value) {
        return new Prosent(value);
    }

    private Prosent(Integer prosent) {
        this.prosent = prosent;
    }

    @Override
    public int hashCode() {
        return prosent != null ? prosent.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Prosent that && Objects.equals(prosent, that.prosent);
    }

    @JsonValue
    public Integer getProsent() {
        return prosent;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [prosent=" + prosent + "]";
    }
}
