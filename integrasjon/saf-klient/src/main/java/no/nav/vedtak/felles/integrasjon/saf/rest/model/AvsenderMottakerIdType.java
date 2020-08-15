package no.nav.vedtak.felles.integrasjon.saf.rest.model;


public enum AvsenderMottakerIdType {
    /**
     * Folkeregisterets fødselsnummer eller d-nummer for en person.
     */
    FNR,
    /**
     * Foretaksregisterets organisasjonsnummer for en juridisk person.
     */
    ORGNR,
    /**
     * Helsepersonellregisterets identifikator for leger og annet helsepersonell.
     */
    HPRNR,
    /**
     * Unik identifikator for utenlandske institusjoner / organisasjoner. Identifikatorene vedlikeholdes i EUs institusjonskatalog.
     */
    UTL_ORG,
    /**
     * AvsenderMottakerId er tom
     */
    NULL,
    /**
     * Ukjent IdType
     */
    UKJENT
}
