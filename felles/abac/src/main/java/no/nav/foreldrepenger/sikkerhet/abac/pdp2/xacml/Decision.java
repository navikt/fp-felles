package no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml;

public enum Decision {
    Permit,
    Deny,
    NotApplicable,
    Indeterminate;

    public String getEksternKode() {
        return name();
    }
}
