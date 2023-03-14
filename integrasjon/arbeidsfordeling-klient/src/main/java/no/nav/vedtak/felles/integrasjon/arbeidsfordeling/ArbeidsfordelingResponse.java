package no.nav.vedtak.felles.integrasjon.arbeidsfordeling;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ArbeidsfordelingResponse(@JsonProperty("enhetNr") String enhetNr, @JsonProperty("navn") String enhetNavn, String status,
                                       @JsonProperty("type") String enhetType) {
}
