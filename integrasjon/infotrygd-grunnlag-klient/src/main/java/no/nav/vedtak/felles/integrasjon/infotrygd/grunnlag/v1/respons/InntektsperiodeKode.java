package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum InntektsperiodeKode {
    @JsonEnumDefaultValue UKJENT,
    M,
    U,
    D,
    Å,
    F,
    X,
    Y
}
