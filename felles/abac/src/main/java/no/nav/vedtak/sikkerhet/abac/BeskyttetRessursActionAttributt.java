package no.nav.vedtak.sikkerhet.abac;

public enum BeskyttetRessursActionAttributt {
    READ("read"),
    UPDATE("update"),
    CREATE("create"),
    DELETE("delete"),

    /**
     * Skal kun brukes av Interceptor
     */
    DUMMY(null);

    private String eksternKode;

    BeskyttetRessursActionAttributt(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}
