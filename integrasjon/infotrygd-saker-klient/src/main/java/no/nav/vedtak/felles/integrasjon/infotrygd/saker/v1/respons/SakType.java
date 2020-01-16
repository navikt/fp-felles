package no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum SakType {
    @JsonEnumDefaultValue
    UKJENT,
    S,
    R,
    K,
    A
}
