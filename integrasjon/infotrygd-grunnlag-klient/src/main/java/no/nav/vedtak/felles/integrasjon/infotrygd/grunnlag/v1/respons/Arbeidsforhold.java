package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.LocalDate;


public record Arbeidsforhold(
    @JsonAlias("arbeidsgiverOrgnr") Orgnummer orgnr,
    @JsonAlias("inntektForPerioden") Integer inntekt,
    Inntektsperiode inntektsperiode,
    Boolean refusjon,
    LocalDate refusjonTom) {
}
