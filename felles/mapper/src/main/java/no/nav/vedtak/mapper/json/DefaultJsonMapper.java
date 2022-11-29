package no.nav.vedtak.mapper.json;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.vedtak.exception.TekniskException;

public class DefaultJsonMapper {

    private DefaultJsonMapper() {

    }

    private static final ObjectMapper MAPPER = createObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"))
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
    }

    //    TODO: Vurder om disse bør med - tas ifm rydding i integrasjon-dtos (mest mulig records)
    //        .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
    //        .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
    //        .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
    //        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    //        .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);

    public static <T> List<T> fromJson(String json, TypeReference<List<T>> typeReference) {
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            throw deserializationException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw deserializationException(e);
        }
    }

    public static <T> T fromJson(URL json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw deserializationException(e);
        }
    }

    public static JsonNode treeFromJson(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw deserializationException(e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (IOException e) {
            throw serializationException(e);
        }
    }

    public static String toPrettyJson(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            throw serializationException(e);
        }
    }

    private static TekniskException deserializationException(IOException e) {
        return new TekniskException("FP-713328", "Fikk IO exception ved deserialisering av JSON", e);
    }

    private static TekniskException serializationException(IOException e) {
        return new TekniskException("F-208314", "Kunne ikke serialisere objekt til JSON", e);
    }
}
