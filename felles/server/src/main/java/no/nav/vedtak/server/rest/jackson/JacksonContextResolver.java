package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.ext.ContextResolver;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import tools.jackson.databind.json.JsonMapper;

public class JacksonContextResolver implements ContextResolver<JsonMapper> {

    private static final JsonMapper MAPPER = DefaultJsonMapper.getJsonMapper();

    @Override
    public JsonMapper getContext(Class<?> type) {
        return MAPPER;
    }

}
