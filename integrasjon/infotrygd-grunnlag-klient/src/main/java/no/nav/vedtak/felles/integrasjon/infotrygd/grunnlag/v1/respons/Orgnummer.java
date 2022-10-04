package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Orgnummer(@JsonValue String orgnr) {

    @JsonCreator
    public static Orgnummer forValue(String orgnr) {
        return new Orgnummer(orgnr);
    }
}
