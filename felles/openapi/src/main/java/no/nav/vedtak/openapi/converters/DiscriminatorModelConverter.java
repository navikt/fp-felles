package no.nav.vedtak.openapi.converters;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;
import tools.jackson.databind.type.SimpleType;

/**
 * DiscriminatorModelConverter adds discriminator property and discriminator mapping when it is missing on types that has been
 * resolved with a non-empty oneOf property.
 * <p>
 * To do this it must map from the resolved oneOf schema ref back to the class it originated from, to get its class name
 * or @JsonTypeName annotations. Therefore, it maintains a lookup map of this where it adds the class type of all resolved
 * schemas with a name.
 */
public class DiscriminatorModelConverter implements ModelConverter {
    private final RefToClassLookup classLookup;

    public DiscriminatorModelConverter(final RefToClassLookup classLookup) {
        this.classLookup = classLookup;
    }

    private JsonTypeInfo getJsonTypeInfo(final AnnotatedType type, final Class<?> typeRawClass) {
        // get @JsonTypeInfo annotation from the class. If there is none, see if the types ctxAnnotations has it. If none
        // found anywhere, throw
        JsonTypeInfo jsonTypeInfo = typeRawClass.getAnnotation(JsonTypeInfo.class);
        if (jsonTypeInfo == null) {
            for(final Annotation annotation : type.getCtxAnnotations()) {
                if (annotation instanceof JsonTypeInfo info) {
                    jsonTypeInfo = info;
                    break;
                }
            }
            if (jsonTypeInfo == null) {
                throw new IllegalArgumentException("superClass " + typeRawClass.getCanonicalName() + " has no @JsonTypeInfo annotation. This is required either on the type itself, or on its container type to determine subtype discriminator");
            }
        }
        // For now we only support use = Id.CLASS and use = Id.NAME. Don't think others are used in our codebase.
        final boolean useClassOrNameDiscriminator = jsonTypeInfo.use() == JsonTypeInfo.Id.CLASS || jsonTypeInfo.use() == JsonTypeInfo.Id.NAME;
        if (!useClassOrNameDiscriminator) {
            throw new IllegalArgumentException("superClass " + typeRawClass.getCanonicalName() + " has unsupported @JsonTypeInfo(use = "+ jsonTypeInfo.use()+" ) value");
        }
        return jsonTypeInfo;
    }

    protected String resolveDiscriminatorProperty(final JsonTypeInfo jsonTypeInfo) {
        // Determine what to set discriminatorProperty to base on @JsonTypeInfo.
        // Note: If we are to support other include options than PROPERTY, this might need changing.
        // if the property value on annotation is set, use that. Else, use the value from the use value.
        final var property = jsonTypeInfo.property();
        if (property != null && !property.isBlank()) {
            return property;
        }
        else {
            return jsonTypeInfo.use().getDefaultPropertyName();
        }
    }

    // A JsonSubType.Type annotation can have several names set for the same class, so this returns a Set.
    protected SortedSet<String> resolveJsonSubTypeNames(final JsonSubTypes jsonSubTypes, final Class<?> subClass) {
        if (jsonSubTypes != null) {
            // Find the Type annotation for given subClass, if there is one.
            final Stream<JsonSubTypes.Type> maybeJsonSubType = Arrays.stream(jsonSubTypes.value()).filter(v -> v.value().equals(subClass));
            // Find name or names value, if defined
            final Stream<String> names = maybeJsonSubType.flatMap(typ -> {
                final var typeNames = typ.names();
                if (typeNames != null && typeNames.length > 0) {
                    return Stream.of(typeNames);
                }
                final var typeName = typ.name();
                if (typeName != null && !typeName.isBlank()) {
                    return Stream.of(typeName);
                }
                return Stream.empty();
            });
            return names.collect(Collectors.toCollection(TreeSet::new));
        }
        return new TreeSet<String>();
    }

    // Resolves name from @JsonTypeName annotation on subClass
    // Returns SortedSet to match return type from resolveJsonSubTypeNames
    protected SortedSet<String> resolveJsonTypeName(final Class<?> subClass) {
        final var ret = new TreeSet<String>();
        final JsonTypeName jsonTypeName = subClass.getDeclaredAnnotation(JsonTypeName.class);
        if (jsonTypeName != null && jsonTypeName.value() != null && !jsonTypeName.value().isBlank()) {
            ret.add(jsonTypeName.value());
        }
        return ret;
    }

    /**
     * We check this because of the comment on JsonTypeInfo.As.EXTERNAL_PROPERTY:
     * "Note that this mechanism can only be used for properties, not for types (classes). Trying to use it for classes
     * will result in inclusion strategy of basic PROPERTY instead."
     */
    private boolean isNotProperty(final AnnotatedType type) {
        return !type.isSchemaProperty();
    }


    protected void fixDiscriminator(final AnnotatedType type, final Class<?> typeRawClass, final Schema superSchema) {
        // If resolved schema has oneOf set, it should also have a discriminator.
        final List<Schema> oneOf = superSchema.getOneOf();
        if (oneOf != null && !oneOf.isEmpty()) {
            final JsonTypeInfo jsonTypeInfo = getJsonTypeInfo(type, typeRawClass);
            // If jsonTypeInfo.include is set to PROPERTY, EXISTING_PROPERTY, or EXTERNAL_PROPERTY on a class (not property),
            // add a discriminator.
            if (
                    jsonTypeInfo.include() == JsonTypeInfo.As.PROPERTY ||
                    jsonTypeInfo.include() == JsonTypeInfo.As.EXISTING_PROPERTY ||
                    (jsonTypeInfo.include() == JsonTypeInfo.As.EXTERNAL_PROPERTY && isNotProperty(type))
            ) {
                Discriminator discriminator = superSchema.getDiscriminator();
                if (discriminator == null) {
                    discriminator = new Discriminator();
                    superSchema.setDiscriminator(discriminator);
                }
                if (discriminator.getPropertyName() == null || discriminator.getPropertyName().isBlank()) {
                    discriminator.setPropertyName(resolveDiscriminatorProperty(jsonTypeInfo));
                }
                // Resolve discriminator mapping if needed. One for each type in the oneOf list
                final JsonSubTypes jsonSubTypes = typeRawClass.getDeclaredAnnotation(JsonSubTypes.class);
                for (final Schema subClassSchema : oneOf) {
                    // Note: If we are to support other values than use = Id.NAME or use = Id.CLASS, this might need changing.
                    // A Discriminator mapping is needed when discriminator field value does not match the schema name or ref.
                    if (jsonTypeInfo.use() == JsonTypeInfo.Id.CLASS) {
                        // it probably does not when use == Id.CLASS. We must then map from the fully qualified class name of
                        // the subClass to the schema ref.
                        final var subClass = this.classLookup.get(subClassSchema.get$ref());
                        if (subClass != null) {
                            discriminator.mapping(subClass.getCanonicalName(), subClassSchema.get$ref());
                        }
                    } else if (jsonTypeInfo.use() == JsonTypeInfo.Id.NAME) {
                        final var subClass = this.classLookup.get(subClassSchema.get$ref());
                        if (subClass != null) {
                            // If use == Id.NAME, two known cases require creating a mapping:
                            // 1: The super type class is annotated with @JsonSubTypes, in which the @JsonSubTypes.Type annotation for the subtype has a name property determining the discriminator property value to use for it.
                            // 2: The subClass is annotated with @JsonTypeName annotation which determines the discriminator property value to use for it.
                            var discriminatorKeyValues = resolveJsonSubTypeNames(jsonSubTypes, subClass);
                            if (discriminatorKeyValues.isEmpty()) { // @JsonTypeName only takes effect if @JsonSubTypes.Type name or names is not set
                                discriminatorKeyValues = resolveJsonTypeName(subClass);
                            }
                            for (final var discriminatorKeyValue : discriminatorKeyValues) {
                                discriminator.mapping(discriminatorKeyValue, subClassSchema.get$ref());
                            }
                        }
                    }
                }
            }
            // XXX: Should perhaps handle other include kinds (WRAPPER, WRAPPER_ARRAY) here. Or throw exception on them.
        }
    }

    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (chain.hasNext()) {
            final Schema resolved = chain.next().resolve(type, context, chain);
            if (resolved != null) {
                final String ref = Optional.ofNullable(resolved.get$ref()).orElse(RefUtils.constructRef(resolved.getName()));
                // Resolved Schema must have name set for us to do anything useful with it.
                final boolean resolvedHasName = resolved.getName() != null && !resolved.getName().isBlank();
                if (type.getType() instanceof SimpleType simpleType) {
                    final Class<?> typeRawClass = simpleType.getRawClass();
                    if (resolvedHasName) {
                        this.classLookup.put(ref, typeRawClass);
                    }

                    // Get the actual Schema when the returned resolved is a ref to it.
                    final Schema resolvedOrReferenced = resolved.get$ref() != null ?
                            context.getDefinedModels().get(RefUtils.extractSimpleName(resolved.get$ref()).getLeft()) :
                            resolved;

                    fixDiscriminator(type, typeRawClass, resolvedOrReferenced);
                } else if (type.getType() instanceof Class<?> cls && resolvedHasName) {
                    this.classLookup.put(ref, cls);
                }
            }
            return resolved;
        }
        return null;
    }
}
