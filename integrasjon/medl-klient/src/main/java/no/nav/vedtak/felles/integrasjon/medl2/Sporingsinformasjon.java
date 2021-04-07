package no.nav.vedtak.felles.integrasjon.medl2;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class Sporingsinformasjon {

    private final LocalDate besluttet;
    private final String kilde;

    @JsonCreator
    public Sporingsinformasjon(@JsonProperty("besluttet") LocalDate besluttet,
            @JsonProperty("kilde") String kilde) {
        this.besluttet = besluttet;
        this.kilde = kilde;
    }

    LocalDate getBesluttet() {
        return besluttet;
    }

    String getKilde() {
        return kilde;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Sporingsinformasjon that = (Sporingsinformasjon) o;
        return Objects.equals(besluttet, that.besluttet) &&
                Objects.equals(kilde, that.kilde);
    }

    @Override
    public int hashCode() {
        return Objects.hash(besluttet, kilde);
    }

    @Override
    public String toString() {
        return "Sporingsinformasjon{" +
                "besluttet=" + besluttet +
                ", kilde='" + kilde + '\'' +
                '}';
    }
}