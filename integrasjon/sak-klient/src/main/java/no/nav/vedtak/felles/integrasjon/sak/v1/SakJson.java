package no.nav.vedtak.felles.integrasjon.sak.v1;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SakJson {

    private Long id;
    private String tema;
    private String applikasjon;
    private String aktoerId;
    private String fagsakNr;

    @JsonCreator
    public SakJson(@JsonProperty("id") Long id,
            @JsonProperty("tema") String tema,
            @JsonProperty("applikasjon") String applikasjon,
            @JsonProperty("aktoerId") String aktoerId,
            @JsonProperty("fagsakNr") String fagsakNr) {
        this.id = id;
        this.tema = tema;
        this.applikasjon = applikasjon;
        this.aktoerId = aktoerId;
        this.fagsakNr = fagsakNr;
    }

    private SakJson() {
    }

    public Long getId() {
        return id;
    }

    public String getTema() {
        return tema;
    }

    public String getApplikasjon() {
        return applikasjon;
    }

    public String getAktoerId() {
        return aktoerId;
    }

    public String getFagsakNr() {
        return fagsakNr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktoerId, applikasjon, fagsakNr, id, tema);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = SakJson.class.cast(o);
        return id == that.id &&
                tema.equals(that.tema)
                && applikasjon.equals(that.applikasjon)
                && aktoerId.equals(that.aktoerId)
                && fagsakNr.equals(that.fagsakNr);

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id + ", tema=" + tema + ", applikasjon=" + applikasjon + ", aktoerId=" + aktoerId
                + ", fagsakNr=" + fagsakNr + "]";
    }

    public static SakJson.Builder getBuilder() {
        return new SakJson.Builder();
    }

    public static class Builder {
        SakJson sak;

        Builder() {
            sak = new SakJson();
        }

        public SakJson.Builder medTema(String tema) {
            this.sak.tema = tema;
            return this;
        }

        public SakJson.Builder medApplikasjon(String applikasjon) {
            this.sak.applikasjon = applikasjon;
            return this;
        }

        public SakJson.Builder medAktoerId(String aktoerId) {
            this.sak.aktoerId = aktoerId;
            return this;
        }

        public SakJson.Builder medFagsakNr(String fagsakNr) {
            this.sak.fagsakNr = fagsakNr;
            return this;
        }

        public SakJson build() {
            Objects.requireNonNull(this.sak.aktoerId);
            Objects.requireNonNull(this.sak.applikasjon);
            return this.sak;
        }
    }

}
