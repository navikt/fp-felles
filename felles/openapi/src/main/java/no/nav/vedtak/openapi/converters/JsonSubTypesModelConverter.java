package no.nav.vedtak.openapi.converters;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import tools.jackson.databind.type.SimpleType;

/**
 * When the type to resolve has @JsonSubTypes annotation, add a @Schema(oneOf = ...) annotation with the subtypes declared
 * in the @JsonSubTypes to the ctxAnnotations on it.
 * <p>
 * The base ModelResolver called last in the chain will then add desired oneOf elements for all the declare subtypes, and
 * we avoid the default behaviour of @JsonSubTypes, which is to also add the allOf schema element on the subtypes.
 * <p>
 * <b>NB:</b> For this to work as intended, the ObjectMapper used by ModelResolver must be set up with the
 * #{@link NoJsonSubTypesAnnotationIntrospector}. Otherwise the base ModelResolver will still add allOf element on subtypes.
 */
public class JsonSubTypesModelConverter implements ModelConverter {

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (chain.hasNext()) {
            if (type.getType() instanceof SimpleType simpleType) {
                final Class<?> cls = simpleType.getRawClass();
                final var incomingSchemaAnnotation = AnnotationUtils.resolveIncomingSchemaAnnotation(type.getCtxAnnotations(), simpleType);
                if (!AnnotationUtils.hasOneOfSchema(incomingSchemaAnnotation)) {
                    // If class has @JsonSubTypes annotation, create @Schema(oneOf = ...) based on it and add it to type.ctxAnnotations
                    final var jsonSubtypesAnnotation = cls.getDeclaredAnnotation(JsonSubTypes.class);
                    if (jsonSubtypesAnnotation != null) {
                        final Set<Class<?>> subClasses = Arrays.stream(jsonSubtypesAnnotation.value()).map(JsonSubTypes.Type::value).collect(Collectors.toUnmodifiableSet());
                        final var oneOfSchema = subClasses.isEmpty() ? null : AnnotationCreator.createOneOfSchemaAnnotation(subClasses);
                        final var outgoingSchema = AnnotationUtils.mergeOneOfInto(incomingSchemaAnnotation, oneOfSchema);
                        final var newCtxAnnotations = AnnotationUtils.replaceSchemaOrArraySchemaAnnotation(type.getCtxAnnotations(), outgoingSchema);
                        return chain.next().resolve(type.ctxAnnotations(newCtxAnnotations), context, chain);
                    }
                }
            }
            return chain.next().resolve(type, context, chain);
        } else {
            return null;
        }
    }
}
