package no.nav.vedtak.sikkerhet.abac.beskyttet;

public enum ResourceType {

    // Til bruk i annotering
    APPLIKASJON,
    DRIFT,
    FAGSAK,
    VENTEFRIST,
    // LOS
    OPPGAVESTYRING_AVDELINGENHET,
    OPPGAVESTYRING,
    // OPPGAVEKØ, TODO: Vurder om skal brukes for å lese oppgaver for LOS. Nå brukes FAGSAK

    // Selvbetjening
    UTTAKSPLAN,

    // Til bruk i annotering for endepunkt som er PIP-tjenester
    PIP,

    /**
     * Skal kun brukes av Interceptor
     */
    DUMMY
}
