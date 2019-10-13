package no.nav.vedtak.log.sporingslogg;

/**
 * Inneholder standard sporingslogg id'er som brukes i Sporingslogg (egen logg som sendes sikkerhetsanalyse).
 * <p>
 * Brukes det kun i <i>din</i> applikasjon - så implementer SporingsloggId og legg til sammen med Abac (eller i egen AbacSporingslogg
 * implementasjon).
 */
public enum StandardSporingsloggId implements SporingsloggId {

    FNR("fnr"),
    AKTOR_ID("aktorId"),

    ABAC_DECISION("decision"),
    ABAC_ACTION("abac_action"),
    ABAC_RESOURCE_TYPE("abac_resource_type"),
    ;

    private String eksternKode;

    StandardSporingsloggId(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    @Override
    public String getSporingsloggKode() {
        return eksternKode;
    }
}
