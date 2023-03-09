package no.nav.foreldrepenger.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.TokenProvider;
import no.nav.vedtak.sikkerhet.kontekst.*;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

import javax.enterprise.context.Dependent;

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
        var kontekst = PROVIDER.getKontekst();
        return kontekst instanceof RequestKontekst rk ? rk.getToken() : null;
    }

    @Override
    public String samlToken() {
        var kontekst = PROVIDER.getKontekst();
        return kontekst instanceof WsRequestKontekst wrk ? wrk.getSamlTokenAsString() : null;
    }
}
