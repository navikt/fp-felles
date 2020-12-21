package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractJerseyOIDCRestClient extends AbstractJerseyRestClient {

    public AbstractJerseyOIDCRestClient() {
        this(mapper);
    }

    public AbstractJerseyOIDCRestClient(ObjectMapper mapper) {
        super(mapper, StandardHeadersRequestFilter.class, OIDCTokenRequestFilter.class);
    }
}
