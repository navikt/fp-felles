package no.nav.vedtak.sikkerhet.abac.beskyttet;

public enum ActionType {
    READ("read"),
    UPDATE("update"),
    CREATE("create"),
    DELETE("delete"),

    /**
     * Skal kun brukes av Interceptor
     */
    DUMMY(null);

    private String eksternKode;

    ActionType(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}
