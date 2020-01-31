package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum BehandlingstemaKode {
    @JsonEnumDefaultValue
    UKJENT,
    AP,
    FP,
    FU,
    FØ,
    SV,
    SP,
    OM,
    PB,
    OP,
    PP,
    PI,
    PN
}
