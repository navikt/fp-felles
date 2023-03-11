package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum StatusKode {
    @JsonEnumDefaultValue UKJENT,
    L,
    A,
    I
}
