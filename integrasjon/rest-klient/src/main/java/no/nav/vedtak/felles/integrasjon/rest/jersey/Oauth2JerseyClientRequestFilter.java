package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

class Oauth2JerseyClientRequestFilter implements ClientRequestFilter {
    private final OAuth2AccessTokenJerseyClient client;

    public Oauth2JerseyClientRequestFilter(OAuth2AccessTokenJerseyClient client) {
        this.client = client;
    }

    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + client.accessToken());
    }

}
