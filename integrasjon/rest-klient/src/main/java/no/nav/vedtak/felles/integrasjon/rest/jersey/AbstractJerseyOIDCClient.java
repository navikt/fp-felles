package no.nav.vedtak.felles.integrasjon.rest.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractJerseyOIDCClient extends AbstractJerseyRestClient {

    public AbstractJerseyOIDCClient() {
        super(StandardHeadersRequestFilter.class, OIDCTokenRequestFilter.class);
    }

    public AbstractJerseyOIDCClient(ObjectMapper mapper) {
        super(mapper, StandardHeadersRequestFilter.class, OIDCTokenRequestFilter.class);
    }
}
