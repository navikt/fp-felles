package no.nav.vedtak.sikkerhet.kontekst;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

import java.util.UUID;

public interface RequestKontekstProvider extends KontekstProvider {

    default OpenIDToken getToken() {
        return getKontekst() instanceof RequestKontekst tk ? tk.getToken() : null;
    }

    default UUID getOid() {
        return getKontekst() instanceof RequestKontekst tk ? tk.getOid() : null;
    }

}
