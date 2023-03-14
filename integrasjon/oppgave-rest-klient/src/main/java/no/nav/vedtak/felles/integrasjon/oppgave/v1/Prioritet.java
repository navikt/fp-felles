package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Prioritet {
    HOY,
    @JsonEnumDefaultValue NORM,
    LAV
}
