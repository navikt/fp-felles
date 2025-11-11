package no.nav.vedtak.mapper.json;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

import no.nav.vedtak.exception.TekniskException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * OBS: JsonMapper er en subklasse av ObjectMapper - tilpasset Json og med en enklere builder
 * Se også jackson-core, pakke json, JsonReadFeature og JsonWriteFeature for mer Java/Json-nær konfig/features
 */
public class DefaultJson3Mapper {

    private DefaultJson3Mapper() {

    }

    private static final JsonMapper MAPPER = createJsonMapperBuilder().build();

    // Foretrekker denne - men bruk heller metoder nedenfor direkte enn å assigne til lokale variable
    public static JsonMapper getJsonMapper() {
        return MAPPER;
    }

    // Bruk denne for kun for ContextResolver (JacksonJsonConfig-klasser) som skal legge til (de)serializers eller registrere subtypes
    public static JsonMapper.Builder getCopyFromDefaultJsonMapper() {
        return createJsonMapperBuilder();
    }

    private static JsonMapper.Builder createJsonMapperBuilder() {
        return JsonMapper.builder()
            .defaultTimeZone(TimeZone.getTimeZone("Europe/Oslo"))
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES) // Var noen tester med null for booleans
            .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES) // TODO: Trengs denne? Sak har kjørt lenge uten
            .changeDefaultPropertyInclusion((a) -> a
                .withValueInclusion(JsonInclude.Include.NON_ABSENT)
                .withContentInclusion(JsonInclude.Include.NON_ABSENT))
            .changeDefaultVisibility((v) -> v
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withCreatorVisibility(JsonAutoDetect.Visibility.ANY)
                    .withScalarConstructorVisibility(JsonAutoDetect.Visibility.ANY));
    }

    public static <T> List<T> fromJson(String json, TypeReference<List<T>> typeReference) {
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (JacksonException e) {
            throw deserializationException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readerFor(clazz).readValue(json);
        } catch (JacksonException e) {
            throw deserializationException(e);
        }
    }

    public static <T> T fromJson(File json, Class<T> clazz) {
        try {
            return MAPPER.readerFor(clazz).readValue(json);
        } catch (JacksonException e) {
            throw deserializationException(e);
        }
    }

    public static <T> List<T> listFromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readerForListOf(clazz).readValue(json);
        } catch (JacksonException e) {
            throw deserializationException(e);
        }
    }

    public static <T> Map<String, T> mapFromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readerForMapOf(clazz).readValue(json);
        } catch (JacksonException e) {
            throw deserializationException(e);
        }
    }

    public static JsonNode treeFromJson(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JacksonException e) {
            throw deserializationException(e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw serializationException(e);
        }
    }

    public static String toPrettyJson(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JacksonException e) {
            throw serializationException(e);
        }
    }

    private static TekniskException deserializationException(JacksonException e) {
        return new TekniskException("FP-713328", "Fikk JacksonException ved deserialisering av JSON", e);
    }

    private static TekniskException serializationException(JacksonException e) {
        return new TekniskException("F-208314", "Kunne ikke serialisere objekt til JSON", e);
    }
}
