package no.nav.vedtak.mapper.json;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.vedtak.exception.TekniskException;

/**
 * OBS: JsonMapper er en subklasse av ObjectMapper - tilpasset Json og med en enklere builder
 * Se også jackson-core, pakke json, JsonReadFeature og JsonWriteFeature for mer Java/Json-nær konfig/features
 */
public class DefaultJsonMapper {

    private DefaultJsonMapper() {

    }

    private static final JsonMapper MAPPER = createJsonMapper();

    @Deprecated // Bruk getJsonMapper() når mapper-konfig brukes as is og ikke endres ((de)serializers eller subtypes
    public static JsonMapper getObjectMapper() {
        return MAPPER;
    }

    // Foretrekker denne - men bruk heller metoder nedenfor direkte enn å assigne til lokale variable
    public static JsonMapper getJsonMapper() {
        return MAPPER;
    }

    // Bruk denne for kun for ContextResolver (JacksonJsonConfig-klasser) som skal legge til (de)serializers eller registrere subtypes
    public static JsonMapper getCopyFromDefaultJsonMapper() {
        return MAPPER.copy();
    }

    private static JsonMapper createJsonMapper() {
        return JsonMapper.builder()
            .addModule(new Jdk8Module())
            .addModule(new JavaTimeModule())
            .defaultTimeZone(TimeZone.getTimeZone("Europe/Oslo"))
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES) // TODO: Trengs denne? Sak har kjørt lenge uten
            .defaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
            .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .visibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY)
            .build();
    }

    public static <T> List<T> fromJson(String json, TypeReference<List<T>> typeReference) {
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            throw deserializationException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readerFor(clazz).readValue(json);
        } catch (IOException e) {
            throw deserializationException(e);
        }
    }

    @Deprecated // Bruk File eller InputStream
    public static <T> T fromJson(URL json, Class<T> clazz) {
        try {
            return MAPPER.readerFor(clazz).readValue(json, clazz);
        } catch (IOException e) {
            throw deserializationException(e);
        }
    }

    public static <T> T fromJson(File json, Class<T> clazz) {
        try {
            return MAPPER.readerFor(clazz).readValue(json, clazz);
        } catch (IOException e) {
            throw deserializationException(e);
        }
    }

    public static <T> List<T> listFromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readerForListOf(clazz).readValue(json);
        } catch (IOException e) {
            throw deserializationException(e);
        }
    }

    public static <T> Map<String, T> mapFromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readerForMapOf(clazz).readValue(json);
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
