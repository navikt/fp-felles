package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Oppgavestatus {
    @JsonEnumDefaultValue
    OPPRETTET,
    AAPNET,
    UNDER_BEHANDLING,
    FERDIGSTILT,
    FEILREGISTRERT;
}
