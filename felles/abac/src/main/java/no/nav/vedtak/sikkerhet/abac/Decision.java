package no.nav.vedtak.sikkerhet.abac;

public enum Decision {
    Permit,
    Deny,
    NotApplicable,
    Indeterminate;

    public String getEksternKode() {
        return name();
    }
}
