package no.nav.vedtak.sikkerhet.kontekst;

public enum SikkerhetContext {
    REQUEST, // Autentisert request m/oidc-token uansett om konsument er bruker, saksbehandler, eller system
    SYSTEM // Intern systemkontekst - typisk prosesstask
}
