package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import com.fasterxml.jackson.annotation.JsonCreator;

public record Prosent(Integer prosent) {

    @JsonCreator
    public static Prosent forValue(Integer value) {
        return new Prosent(value);
    }
}
