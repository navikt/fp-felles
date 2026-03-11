package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.ext.ContextResolver;

import no.nav.vedtak.mapper.json.DefaultJson3Mapper;
import tools.jackson.databind.json.JsonMapper;

public class Jackson3Mapper implements ContextResolver<JsonMapper> {

    private static final JsonMapper MAPPER = DefaultJson3Mapper.getJsonMapper();

    @Override
    public JsonMapper getContext(Class<?> type) {
        return MAPPER;
    }

}
