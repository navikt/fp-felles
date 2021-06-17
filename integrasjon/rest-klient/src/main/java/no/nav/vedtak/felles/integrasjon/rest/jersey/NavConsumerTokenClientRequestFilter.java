package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.NAV_CONSUMER_TOKEN_HEADER;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;

import javax.ws.rs.client.ClientRequestContext;

public class NavConsumerTokenClientRequestFilter extends OidcTokenRequestFilter {

    @Override
    public void filter(ClientRequestContext ctx) {
        ctx.getHeaders().add(NAV_CONSUMER_TOKEN_HEADER, OIDC_AUTH_HEADER_PREFIX + accessToken());
    }
}
