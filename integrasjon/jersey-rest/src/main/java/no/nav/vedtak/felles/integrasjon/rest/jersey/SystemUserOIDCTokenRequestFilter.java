package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.isso.SystemUserIdTokenProvider.getSystemUserIdToken;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

class SystemUserOIDCTokenRequestFilter implements ClientRequestFilter, AccessTokenProvider {

    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + accessToken());
    }

    @Override
    public String accessToken() {
        return getSystemUserIdToken().getToken();
    }
}
