package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;

import javax.ws.rs.client.ClientRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Denne klassen erstatter AbstractOidcRestClient
 */
public class AbstractJerseyOidcRestClient extends AbstractJerseyRestClient {

    public AbstractJerseyOidcRestClient() {
        this(mapper, StandardHeadersRequestFilter.class, OidcTokenRequestFilter.class);
    }

    public AbstractJerseyOidcRestClient(ClientRequestFilter... filters) {
        super(mapper, filters);
    }

    public AbstractJerseyOidcRestClient(Class<? extends ClientRequestFilter>... filters) {
        this(mapper, filters);
    }

    public AbstractJerseyOidcRestClient(ObjectMapper mapper, Class<? extends ClientRequestFilter>... filters) {
        super(mapper, filters);
    }

}
