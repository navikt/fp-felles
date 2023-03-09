package no.nav.vedtak.sikkerhet.abac.beskyttet;

import static no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter.*;

public enum ResourceType {

    // Til bruk i annotering
    APPLIKASJON(RESOURCE_TYPE_FP_APPLIKASJON),
    BATCH(RESOURCE_TYPE_FP_BATCH),
    DRIFT(RESOURCE_TYPE_FP_DRIFT),
    FAGSAK(RESOURCE_TYPE_FP_FAGSAK),
    VENTEFRIST(RESOURCE_TYPE_FP_VENTEFRIST),
    // LOS
    OPPGAVESTYRING_AVDELINGENHET(RESOURCE_TYPE_FP_AVDELINGENHET),
    OPPGAVESTYRING(RESOURCE_TYPE_FP_OPPGAVESTYRING),
    OPPGAVEKØ(RESOURCE_TYPE_FP_OPPGAVEKØ),
    SAKLISTE(RESOURCE_TYPE_FP_SAKLISTE),
    // Risk
    RISIKOKLASSIFISERING(RESOURCE_TYPE_FP_RISIKOKLASSIFISERING),
    // Selvbetjening
    UTTAKSPLAN(RESOURCE_TYPE_FP_UTTAKSPLAN),

    // Til bruk i annotering for endepunkt som er PIP-tjenester
    PIP(RESOURCE_TYPE_INTERNAL_PIP),

    /**
     * Skal kun brukes av Interceptor
     */
    DUMMY("");

    private final String resourceTypeAttribute;

    ResourceType(String resourceTypeAttribute) {
        this.resourceTypeAttribute = resourceTypeAttribute;
    }

    public String getResourceTypeAttribute() {
        return this != DUMMY ? resourceTypeAttribute : null;
    }
}
