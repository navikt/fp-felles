package no.nav.vedtak.felles.integrasjon.saf.rest.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Dokumentvariant {

    @JsonProperty("variantformat")
    private VariantFormat variantFormat;
    @JsonProperty("filnavn")
    private String filnavn;
    @JsonProperty("filtype")
    private String filtype;
    @JsonProperty("saksbehandlerHarTilgang")
    private Boolean saksbehandlerHarTilgang;

    @JsonCreator
    public Dokumentvariant(@JsonProperty("variantformat") VariantFormat variantFormat, @JsonProperty("filnavn") String filnavn,
                           @JsonProperty("filtype") String filtype, @JsonProperty("saksbehandlerHarTilgang") Boolean saksbehandlerHarTilgang) {
        this.variantFormat = variantFormat;
        this.filnavn = filnavn;
        this.filtype = filtype;
        this.saksbehandlerHarTilgang = saksbehandlerHarTilgang;
    }

    public VariantFormat getVariantFormat() {
        return variantFormat;
    }

    public String getFilnavn() {
        return filnavn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dokumentvariant that = (Dokumentvariant) o;
        return variantFormat == that.variantFormat &&
                Objects.equals(filnavn, that.filnavn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantFormat, filnavn);
    }

    @Override
    public String toString() {
        return "Dokumentvariant{" +
                "variantFormat=" + variantFormat +
                ", filnavn='" + filnavn + '\'' +
                '}';
    }

    public String getFiltype() {
        return filtype;
    }

    public Boolean getSaksbehandlerHarTilgang() {
        return saksbehandlerHarTilgang;
    }
}
