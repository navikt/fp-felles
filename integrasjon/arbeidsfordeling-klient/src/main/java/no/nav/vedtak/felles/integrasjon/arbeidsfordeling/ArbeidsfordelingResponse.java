package no.nav.vedtak.felles.integrasjon.arbeidsfordeling;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ArbeidsfordelingResponse(@JsonProperty("enhetNr") String enhetNr,
        @JsonProperty("navn") String enhetNavn,
        @JsonProperty("status") String status,
        @JsonProperty("type") String enhetType) {
}
