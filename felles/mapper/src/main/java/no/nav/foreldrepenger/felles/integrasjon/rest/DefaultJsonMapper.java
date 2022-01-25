package no.nav.foreldrepenger.felles.integrasjon.rest;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.exception.TekniskException;

@Deprecated // Bruk no.nav.vedtak.mapper.json.DefaultJsonMapper
public class DefaultJsonMapper {

    private DefaultJsonMapper() {

    }

    public static final ObjectMapper MAPPER = no.nav.vedtak.mapper.json.DefaultJsonMapper.getObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    public static <T> List<T> fromJson(String json, TypeReference<List<T>> typeReference) {
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            throw new TekniskException("F-919328", "Fikk IO exception ved parsing av JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new TekniskException("FP-713328", "Fikk IO exception ved deserialisering av JSON", e);
        }
    }

    public static JsonNode treeFromJson(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new TekniskException("F-919328", "Fikk IO exception ved parsing av JSON", e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (IOException e) {
            throw new TekniskException("F-208314", "Kunne ikke serialisere objekt til JSON", e);
        }
    }
}
