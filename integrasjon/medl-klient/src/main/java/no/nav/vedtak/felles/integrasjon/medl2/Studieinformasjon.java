package no.nav.vedtak.felles.integrasjon.medl2;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class Studieinformasjon {

    @JsonProperty("studieland")
    private final String studieland;

    @JsonCreator
    public Studieinformasjon(@JsonProperty("studieland") String studieland) {
        this.studieland = studieland;

    }

    String getStudieland() {
        return studieland;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Studieinformasjon that = (Studieinformasjon) o;
        return Objects.equals(studieland, that.studieland);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studieland);
    }

    @Override
    public String toString() {
        return "Studieinformasjon{" +
                "studieland='" + studieland + '\'' +
                '}';
    }
}