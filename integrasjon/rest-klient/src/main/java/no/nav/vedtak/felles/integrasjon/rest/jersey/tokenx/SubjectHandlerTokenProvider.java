package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.util.Optional;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;

public class SubjectHandlerTokenProvider implements TokenProvider {


    @Override
    public String getToken() {
        return Optional.ofNullable(no.nav.vedtak.sikkerhet.oidc.token.TokenProvider.getTokenFor(SikkerhetContext.BRUKER))
            .map(OpenIDToken::token)
            .orElseThrow();
    }

}
