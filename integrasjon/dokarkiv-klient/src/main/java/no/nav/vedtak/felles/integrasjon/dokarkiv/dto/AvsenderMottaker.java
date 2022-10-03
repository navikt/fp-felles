package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

public record AvsenderMottaker(String id, AvsenderMottakerIdType idType, String navn) {

    public enum AvsenderMottakerIdType {
        UKJENT,
        FNR,
        ORGNR
    }
}
