package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;


public record Arbeidsforhold(
    @JsonAlias("arbeidsgiverOrgnr") Orgnummer orgnr,
    @JsonAlias("inntektForPerioden") Integer inntekt,
    Inntektsperiode inntektsperiode,
    Boolean refusjon,
    LocalDate refusjonTom) {


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
        return inntektsperiode();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public Boolean getRefusjon() {
        return refusjon();
    }
}
