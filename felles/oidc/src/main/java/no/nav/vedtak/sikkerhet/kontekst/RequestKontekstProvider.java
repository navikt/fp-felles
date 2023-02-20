package no.nav.vedtak.sikkerhet.kontekst;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public interface RequestKontekstProvider extends KontekstProvider {

    default OpenIDToken getToken() {
        return getKontekst() instanceof RequestKontekst tk ? tk.getToken() : null;
    }

}
