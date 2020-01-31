package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Arbeidskategori {
    private final ArbeidskategoriKode kode;
    private final String termnavn;

    @JsonCreator
    public Arbeidskategori(@JsonProperty("kode") ArbeidskategoriKode kode, @JsonProperty("termnavn") String termnavn) {
        this.kode = kode;
        this.termnavn = termnavn;
    }

    public ArbeidskategoriKode getKode() {
        return kode;
    }

    public String getTermnavn() {
        return termnavn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kode, termnavn);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Arbeidskategori)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Arbeidskategori that = (Arbeidskategori) obj;
        return Objects.equals(that.kode, this.kode) &&
                Objects.equals(that.termnavn, this.termnavn);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[kode=" + kode + ", termnavn=" + termnavn + "]";
    }

}
