package no.nav.vedtak.openapi.converters;

import java.util.Arrays;
import java.util.Iterator;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import tools.jackson.databind.type.SimpleType;

/**
 * For alle enum verdier som blir definert i schema, legg til x-enum-varnames liste med namn på enum verdiane.
 * <p>
 * Dette for at generert (typescript) klientkode i neste omgang skal generere const objekter og ha property namn
 * med samme verdi som java kode har definert for enum verdiane.
 */
public class EnumVarnamesConverter implements ModelConverter {
    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (chain.hasNext()) {
            // First, resolve the schema for current type
            final var resolvedSchema = chain.next().resolve(type, context, chain);
            // If resolved schema type has enum values, resolve and add x-enum-varnames
            if (resolvedSchema != null && type.getType() instanceof SimpleType simpleType && simpleType.isEnumType()) {
                final var createdEnumSchema = context.resolve(type);
                if (createdEnumSchema != null && createdEnumSchema.getEnum() != null && !createdEnumSchema.getEnum().isEmpty()) {
                    // Find enum name values, and add them to "x-enum-varnames" extension.
                    // This makes the generated enum consts have the same property names as the java enums has declared.
                    final var enumValues = createdEnumSchema.getEnum();
                    final Class<Enum> enumClass = (Class<Enum>) simpleType.getRawClass();
                    final String[] names = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray(String[]::new);
                    if (names.length != enumValues.size()) {
                        throw new RuntimeException("resolved enum values count ("+enumValues.size()+") not equal constructed enum class constants count ("+names.length+")");
                    }
                    createdEnumSchema.addExtension("x-enum-varnames", names);
                }
            }

            return resolvedSchema;
        } else {
            return null;
        }
    }
}
