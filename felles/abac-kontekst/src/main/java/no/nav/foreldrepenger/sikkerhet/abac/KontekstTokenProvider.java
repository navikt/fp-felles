package no.nav.foreldrepenger.sikkerhet.abac;

import jakarta.enterprise.context.Dependent;
import no.nav.vedtak.sikkerhet.abac.TokenProvider;
import no.nav.vedtak.sikkerhet.kontekst.DefaultRequestKontekstProvider;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;
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
        var kontekst = PROVIDER.getKontekst();
        return kontekst instanceof RequestKontekst rk ? rk.getToken() : null;
    }
}
