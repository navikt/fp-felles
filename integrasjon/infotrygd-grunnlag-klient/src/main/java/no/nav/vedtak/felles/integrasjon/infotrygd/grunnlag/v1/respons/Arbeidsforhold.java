package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import com.fasterxml.jackson.annotation.JsonAlias;



public record Arbeidsforhold(
        @JsonAlias("arbeidsgiverOrgnr") Orgnummer orgnr,
        @JsonAlias("inntektForPerioden") Integer inntekt,
        Inntektsperiode inntektperiode,
        Boolean refusjon) {


    @Deprecated(since = "4.0.x", forRemoval = true)
    public Orgnummer getOrgnr() {
        return orgnr();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public Integer getInntekt() {
        return inntekt();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public Inntektsperiode getInntektperiode() {
        return inntektperiode();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public Boolean getRefusjon() {
        return refusjon();
    }
}
