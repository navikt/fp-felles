package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;

import javax.ws.rs.client.ClientRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractJerseyOidcRestClient extends AbstractJerseyRestClient {

    public AbstractJerseyOidcRestClient() {
        this(mapper, new StandardHeadersRequestFilter(), new OidcTokenRequestFilter());
    }

    public AbstractJerseyOidcRestClient(ClientRequestFilter... filters) {
        this(mapper, filters);
    }

    public AbstractJerseyOidcRestClient(ObjectMapper mapper, ClientRequestFilter... filters) {
        super(mapper, filters);
    }

}
