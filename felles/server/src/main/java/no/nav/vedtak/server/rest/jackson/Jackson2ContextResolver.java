package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.json.JsonMapper;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

public class Jackson2ContextResolver implements ContextResolver<JsonMapper> {

    private static final JsonMapper MAPPER = DefaultJsonMapper.getJsonMapper();

    @Override
    public JsonMapper getContext(Class<?> type) {
        return MAPPER;
    }

}
