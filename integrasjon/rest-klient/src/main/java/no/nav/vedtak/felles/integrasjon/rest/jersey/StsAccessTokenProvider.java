package no.nav.vedtak.felles.integrasjon.rest.jersey;

import javax.enterprise.context.ApplicationScoped;

import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

@ApplicationScoped
public class StsAccessTokenProvider {

    StsAccessTokenProvider() {
    }

    public String accessToken() {
        return TokenProvider.getTokenFor(SikkerhetContext.BRUKER).token();
    }

    public String systemToken() {
        return TokenProvider.getStsSystemToken().token();
    }
}
