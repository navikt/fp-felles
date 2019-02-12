package no.nav.vedtak.sikkerhet.pdp.xacml;

public enum Advice {
    DENY_KODE_6("deny_kode6"),
    DENY_KODE_7("deny_kode7"),
    DENY_EGEN_ANSATT("deny_egen_ansatt");

    private final String eksternKode;

    Advice(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}
