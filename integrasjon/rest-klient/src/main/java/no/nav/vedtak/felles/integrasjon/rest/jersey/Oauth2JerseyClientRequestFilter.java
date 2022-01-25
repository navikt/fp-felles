package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

class Oauth2JerseyClientRequestFilter implements ClientRequestFilter {

    private String scope;

    public Oauth2JerseyClientRequestFilter(String scope) {
        this.scope = scope;
    }

    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + TokenProvider.getAzureSystemToken(scope).token());
    }

}
