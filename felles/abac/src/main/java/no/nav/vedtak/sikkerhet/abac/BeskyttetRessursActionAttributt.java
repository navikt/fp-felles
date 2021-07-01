package no.nav.vedtak.sikkerhet.abac;

public enum BeskyttetRessursActionAttributt {
    READ,
    UPDATE,
    CREATE,
    DELETE,

    /**
     * Skal kun brukes av Interceptor
     */
    DUMMY;

    public String getEksternKode() {
        return this != DUMMY ? name().toLowerCase() : null;
    }
}
