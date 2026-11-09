package no.nav.vedtak.openapi.converters;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Set;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.media.DependentRequired;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;

public class AnnotationCreator {

    private AnnotationCreator() {
    }

    /**
     * Programmatically creates an instance of swagger @Schema annotation with given oneOf value
     */
    public static io.swagger.v3.oas.annotations.media.Schema createOneOfSchemaAnnotation(Set<Class<?>> oneOf) {
        final var sortedOneOf = oneOf.stream()
                .sorted(Comparator.comparing(Class::getCanonicalName))
                .toArray(Class<?>[]::new);

        io.swagger.v3.oas.annotations.media.Schema schema = new io.swagger.v3.oas.annotations.media.Schema() {
            @Override
            public Class<?> implementation() {
                return Void.class;
            }

            @Override
            public Class<?> not() {
                return Void.class;
            }

            @Override
            public Class<?>[] oneOf() {
                return sortedOneOf;
            }

            @Override
            public Class<?>[] anyOf() {
                return new Class[0];
            }

            @Override
            public Class<?>[] allOf() {
                return new Class[0];
            }

            @Override
            public String name() {
                return "";
            }

            @Override
            public String title() {
                return "";
            }

            @Override
            public double multipleOf() {
                return 0;
            }

            @Override
            public String maximum() {
                return "";
            }

            @Override
            public boolean exclusiveMaximum() {
                return false;
            }

            @Override
            public String minimum() {
                return "";
            }

            @Override
            public boolean exclusiveMinimum() {
                return false;
            }

            @Override
            public int maxLength() {
                return Integer.MAX_VALUE;
            }

            @Override
            public int minLength() {
                return 0;
            }

            @Override
            public String pattern() {
                return "";
            }

            @Override
            public int maxProperties() {
                return 0;
            }

            @Override
            public int minProperties() {
                return 0;
            }

            @Override
            public String[] requiredProperties() {
                return new String[0];
            }

            @Override
            public boolean required() {
                return false;
            }

            @Override
            public RequiredMode requiredMode() {
                return RequiredMode.AUTO;
            }

            @Override
            public String description() {
                return "";
            }

            @Override
            public String format() {
                return "";
            }

            @Override
            public String ref() {
                return "";
            }

            @Override
            public boolean nullable() {
                return false;
            }

            @Override
            public boolean readOnly() {
                return false;
            }

            @Override
            public boolean writeOnly() {
                return false;
            }

            @Override
            public AccessMode accessMode() {
                return AccessMode.AUTO;
            }

            @Override
            public String example() {
                return "";
            }

            @Override
            public ExternalDocumentation externalDocs() {
                return new ExternalDocumentation() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ExternalDocumentation.class;
                    }

                    @Override
                    public String description() {
                        return "";
                    }

                    @Override
                    public String url() {
                        return "";
                    }

                    @Override
                    public Extension[] extensions() {
                        return new Extension[0];
                    }
                };
            }

            @Override
            public boolean deprecated() {
                return false;
            }

            @Override
            public String type() {
                return "";
            }

            @Override
            public String[] allowableValues() {
                return new String[0];
            }

            @Override
            public String defaultValue() {
                return "";
            }

            @Override
            public String discriminatorProperty() {
                return "";
            }

            @Override
            public DiscriminatorMapping[] discriminatorMapping() {
                return new DiscriminatorMapping[0];
            }

            @Override
            public boolean hidden() {
                return false;
            }

            @Override
            public boolean enumAsRef() {
                return true;
            }

            @Override
            public Class<?>[] subTypes() {
                return new Class[0];
            }

            @Override
            public Extension[] extensions() {
                return new Extension[0];
            }

            @Override
            public Class<?>[] prefixItems() {
                return new Class[0];
            }

            @Override
            public String[] types() {
                return new String[0];
            }

            @Override
            public int exclusiveMaximumValue() {
                return 0;
            }

            @Override
            public int exclusiveMinimumValue() {
                return 0;
            }

            @Override
            public Class<?> contains() {
                return Void.class;
            }

            @Override
            public String $id() {
                return "";
            }

            @Override
            public String $schema() {
                return "";
            }

            @Override
            public String $anchor() {
                return "";
            }

            @Override
            public String $vocabulary() {
                return "";
            }

            @Override
            public String $dynamicAnchor() {
                return "";
            }

            @Override
            public String $dynamicRef() {
                return "";
            }

            @Override
            public String contentEncoding() {
                return "";
            }

            @Override
            public String contentMediaType() {
                return "";
            }

            @Override
            public Class<?> contentSchema() {
                return Void.class;
            }

            @Override
            public Class<?> propertyNames() {
                return Void.class;
            }

            @Override
            public int maxContains() {
                return Integer.MAX_VALUE;
            }

            @Override
            public int minContains() {
                return 0;
            }

            @Override
            public Class<?> additionalItems() {
                return Void.class;
            }

            @Override
            public Class<?> unevaluatedItems() {
                return Void.class;
            }

            @Override
            public Class<?> _if () {
                return Void.class;
            }

            @Override
            public Class<?> _else() {
                return Void.class;
            }

            @Override
            public Class<?> then() {
                return Void.class;
            }

            @Override
            public String $comment() {
                return "";
            }

            @Override
            public Class<?>[] exampleClasses() {
                return new Class[0];
            }

            @Override
            public AdditionalPropertiesValue additionalProperties() {
                return AdditionalPropertiesValue.USE_ADDITIONAL_PROPERTIES_ANNOTATION;
            }

            @Override
            public DependentRequired[] dependentRequiredMap() {
                return new DependentRequired[0];
            }

            @Override
            public StringToClassMapItem[] dependentSchemas() {
                return new StringToClassMapItem[0];
            }

            @Override
            public StringToClassMapItem[] patternProperties() {
                return new StringToClassMapItem[0];
            }

            @Override
            public StringToClassMapItem[] properties() {
                return new StringToClassMapItem[0];
            }

            @Override
            public Class<?> unevaluatedProperties() {
                return Void.class;
            }

            @Override
            public Class<?> additionalPropertiesSchema() {
                return Void.class;
            }

            @Override
            public String[] examples() {
                return new String[0];
            }

            @Override
            public String _const() {
                return "";
            }

            @Override
            public SchemaResolution schemaResolution() {
                return SchemaResolution.AUTO;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return io.swagger.v3.oas.annotations.media.Schema.class;
            }

        };
        return schema;
    }

}
