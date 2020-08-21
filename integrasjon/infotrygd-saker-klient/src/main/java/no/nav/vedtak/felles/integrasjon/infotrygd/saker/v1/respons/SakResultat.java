package no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum SakResultat {
    @JsonEnumDefaultValue
    UKJENT,
    A,
    AK,
    AV,
    DI,
    DT,
    FB,
    FI,
    H,
    HB,
    I,
    IN,
    IS,
    IT,
    MO,
    MT,
    NB,
    O,
    PA,
    R,
    SB,
    TB,
    TH,
    TO,
    UB,
    Ø;
}
