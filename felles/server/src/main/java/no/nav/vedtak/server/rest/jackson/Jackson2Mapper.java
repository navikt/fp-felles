package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

public class Jackson2Mapper implements ContextResolver<ObjectMapper> {

    private static final ObjectMapper MAPPER = DefaultJsonMapper.getJsonMapper();


    public Jackson2Mapper() {
        // CDI
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return MAPPER;
    }

}
