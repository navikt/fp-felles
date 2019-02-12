package no.nav.vedtak.sikkerhet.domene;

public enum IdentType {
    // Case defined by NAV Standard, the strings are passed in SAML-tokens
    Systemressurs, // NOSONAR
    EksternBruker, // NOSONAR
    InternBruker, // NOSONAR
    Samhandler, // NOSONAR
    Sikkerhet, // NOSONAR
    Prosess // NOSONAR
}
