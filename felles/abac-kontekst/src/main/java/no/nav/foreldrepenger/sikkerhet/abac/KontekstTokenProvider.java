package no.nav.foreldrepenger.sikkerhet.abac;

import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.Dependent;

import no.nav.vedtak.sikkerhet.abac.TokenProvider;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.DefaultRequestKontekstProvider;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekstProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

@Dependent
public class KontekstTokenProvider implements TokenProvider {

    private static final RequestKontekstProvider PROVIDER = new DefaultRequestKontekstProvider();

    @Override
    public String getUid() {
        return PROVIDER.getKontekst().getUid();
    }

    @Override
    public IdentType getIdentType() {
        return PROVIDER.getKontekst().getIdentType();
    }

    @Override
    public OpenIDToken openIdToken() {
        return PROVIDER.getToken();
    }

    @Override
    public UUID getOid() {
        return PROVIDER.getOid();
    }

    @Override
    public Set<AnsattGruppe> getAnsattGrupper() {
        return PROVIDER.getAnsattGrupper();
    }
}
