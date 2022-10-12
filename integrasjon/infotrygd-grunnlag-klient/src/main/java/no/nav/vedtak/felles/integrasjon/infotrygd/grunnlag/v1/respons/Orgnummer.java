package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import com.fasterxml.jackson.annotation.JsonCreator;

public record Orgnummer(String orgnr) {

    @JsonCreator
    public static Orgnummer forValue(String orgnr) {
        return new Orgnummer(orgnr);
    }
}
