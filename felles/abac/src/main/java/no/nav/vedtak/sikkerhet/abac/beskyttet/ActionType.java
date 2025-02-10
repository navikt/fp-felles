package no.nav.vedtak.sikkerhet.abac.beskyttet;

public enum ActionType {
    READ,
    UPDATE,
    CREATE,
    DELETE,

    /**
     * Skal kun brukes av Interceptor
     */
    DUMMY;

}
