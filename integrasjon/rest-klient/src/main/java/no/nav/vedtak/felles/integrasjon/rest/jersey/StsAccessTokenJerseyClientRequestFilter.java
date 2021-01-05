package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.HttpHeaders.CACHE_CONTROL;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CONSUMERID;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
class StsAccessTokenJerseyClientRequestFilter implements ClientRequestFilter {

    private final String consumerId;

    public StsAccessTokenJerseyClientRequestFilter(String consumerId) {
        this.consumerId = consumerId;
    }

    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        ctx.getHeaders().add(CACHE_CONTROL, "no-cache");
        ctx.getHeaders().add(DEFAULT_NAV_CONSUMERID, consumerId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [consumerId=" + consumerId + "]";
    }
}
