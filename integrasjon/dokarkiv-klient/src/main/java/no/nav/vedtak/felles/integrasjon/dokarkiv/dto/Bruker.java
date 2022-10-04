package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public record Bruker(String id, BrukerIdType idType) {

    public enum BrukerIdType {
        @JsonEnumDefaultValue
        UKJENT,
        AKTOERID,
        FNR,
        ORGNR
    }
}
