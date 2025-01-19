package no.nav.vedtak.sikkerhet.kontekst;

import java.util.Set;
import java.util.UUID;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public interface RequestKontekstProvider extends KontekstProvider {

    default OpenIDToken getToken() {
        return getKontekst() instanceof RequestKontekst tk ? tk.getToken() : null;
    }

    default UUID getOid() {
        return getKontekst() instanceof RequestKontekst tk ? tk.getOid() : null;
    }

    default Set<AnsattGruppe> getAnsattGrupper() {
        return getKontekst() instanceof RequestKontekst tk ? tk.getGrupper() : Set.of();
    }

}
