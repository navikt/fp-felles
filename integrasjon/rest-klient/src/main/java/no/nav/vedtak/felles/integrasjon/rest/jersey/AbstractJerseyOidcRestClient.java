package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractJerseyOidcRestClient extends AbstractJerseyRestClient {

    public AbstractJerseyOidcRestClient() {
        this(mapper);
    }

    public AbstractJerseyOidcRestClient(ObjectMapper mapper) {
        super(mapper, StandardHeadersRequestFilter.class, OidcTokenRequestFilter.class);
    }
}
