package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;



public record Arbeidsforhold(
            @JsonProperty("orgnr") @JsonAlias("arbeidsgiverOrgnr") Orgnummer orgnr,
            @JsonProperty("inntekt") @JsonAlias("inntektForPerioden") Integer inntekt,
            @JsonProperty("inntektsperiode") Inntektsperiode inntektperiode,
            @JsonProperty("refusjon") Boolean refusjon) {


    @Deprecated
    public Orgnummer getOrgnr() {
        return orgnr();
    }

    @Deprecated
    public Integer getInntekt() {
        return inntekt();
    }

    @Deprecated
    public Inntektsperiode getInntektperiode() {
        return inntektperiode();
    }

    @Deprecated
    public Boolean getRefusjon() {
        return refusjon();
    }
}
