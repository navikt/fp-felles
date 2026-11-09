package no.nav.vedtak.openapi.converters;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;

import io.swagger.v3.core.util.AnnotationsUtils;
import tools.jackson.databind.type.SimpleType;

public class AnnotationUtils {

    private AnnotationUtils() {
    }

    public static boolean hasOneOfSchema(final Annotation incoming) {
        if (incoming instanceof io.swagger.v3.oas.annotations.media.Schema schema) {
            return schema.oneOf() != null && schema.oneOf().length > 0;
        } else if (incoming instanceof io.swagger.v3.oas.annotations.media.ArraySchema arraySchema) {
            final var schema = arraySchema.schema();
            return schema.oneOf() != null && schema.oneOf().length > 0;
        } else {
            return false;
        }
    }

    /**
     * Adds oneOfSchema content to incoming, unless the incoming Schema annotation already has the property set in oneOfSchema. Will do nothing then
     */
    public static Annotation mergeOneOfInto(final Annotation incoming, final io.swagger.v3.oas.annotations.media.Schema oneOfSchema) {
        if (incoming == null) {
            return oneOfSchema;
        } else if (oneOfSchema == null) {
            return incoming;
        } else if (incoming instanceof io.swagger.v3.oas.annotations.media.Schema schema) {
            return AnnotationsUtils.mergeSchemaAnnotations(schema, oneOfSchema);
        } else if (incoming instanceof io.swagger.v3.oas.annotations.media.ArraySchema arraySchema) {
            final var mergedSchema = AnnotationsUtils.mergeSchemaAnnotations(arraySchema.schema(), oneOfSchema);
            return AnnotationsUtils.mergeArrayWithSchemaAnnotation(arraySchema, mergedSchema);
        } else {
            throw new IllegalArgumentException("incoming not instance of io.swagger.v3.oas.annotations.media.Schema or io.swagger.v3.oas.annotations.media.ArraySchema. Was: " + incoming.getClass().getCanonicalName() + ". Cannot merge oneOf Schema into it");
        }
    }

    public static Annotation[] replaceSchemaOrArraySchemaAnnotation(final Annotation[] original, final Annotation replacement) {
        if (replacement == null) {
            return original;
        }
        if (original == null) {
            return new Annotation[]{replacement};
        }
        final var list = new ArrayList<Annotation>(Arrays.asList(original));
        var replaced = false;
        for(var i = 0; i < list.size(); i++) {
            if (list.get(i).annotationType().equals(replacement.annotationType())) {
                list.set(i, replacement);
                replaced = true;
            }
        }
        if (!replaced) {
            // Add it to end of list
            list.add(replacement);
        }
        return list.toArray(new Annotation[]{});
    }

    /**
     * Resolves Schema annotation from incoming ctxAnnotations and/or the given type.
     * If the incomingCtxAnnotations are equal to the newResolved, return incomingCtxAnnotations, to avoid creating new
     * objects, that cause stackoverflow in the context.resolve code.
     */
    public static Annotation resolveIncomingSchemaAnnotation(final Annotation[] incomingCtxAnnotations, final SimpleType simpleType) {
        final var newResolved = AnnotationsUtils.mergeSchemaAnnotations(incomingCtxAnnotations, simpleType, false);
        Annotation incomingCtxSchemaAnnotation = AnnotationsUtils.getSchemaAnnotation(incomingCtxAnnotations);
        if (incomingCtxSchemaAnnotation == null) {
            incomingCtxSchemaAnnotation = AnnotationsUtils.getArraySchemaAnnotation(incomingCtxAnnotations);
        }
        if (incomingCtxSchemaAnnotation != null && AnnotationsUtils.equals(incomingCtxSchemaAnnotation, newResolved)){
            return incomingCtxSchemaAnnotation;
        }
        return newResolved;
    }


}
