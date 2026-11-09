package no.nav.vedtak.openapi;

import java.util.Set;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.jackson.TypeNameResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.rest.Reader;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.openapi.converters.DiscriminatorModelConverter;
import no.nav.vedtak.openapi.converters.EnumVarnamesConverter;
import no.nav.vedtak.openapi.converters.JsonSubTypesModelConverter;
import no.nav.vedtak.openapi.converters.NoJsonSubTypesAnnotationIntrospector;
import no.nav.vedtak.openapi.converters.RefToClassLookup;
import no.nav.vedtak.openapi.converters.RegisteredSubtypesModelConverter;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Custom Reader for api som går ut mot typegenereing. Utenom vanlig reader så gjør denne:
 *  1) Her fjerner vi path (vi ønsker ikke mulighet til å kalle endepunktene via swagger)
 *  2) Registere ModulConverter med egendefinert Objectmapper i henhold til hva frontend forventer
 */
public class TypegenereringOpenApiReader extends Reader {

    @Override
    public OpenAPI read(Set<Class<?>> resourceClasses) {
        settOppForTypegenereringFrontend(new FellesTypeNameResolver(), Set.of());
        var openApi = super.read(resourceClasses);
        openApi.setPaths(new Paths());
        return openApi;
    }

    private static class FellesTypeNameResolver extends TypeNameResolver {
        @Override
        public String getNameOfClass(Class<?> cls) {
            return cls.getName();
        }
    }

    public static void settOppForTypegenereringFrontend() {
        settOppForTypegenereringFrontend(new FellesTypeNameResolver(), Set.of());
    }

    public static void settOppForTypegenereringFrontend(TypeNameResolver typeNameResolver) {
        settOppForTypegenereringFrontend(typeNameResolver, Set.of());
    }

    public static void settOppForTypegenereringFrontend(Set<Class<?>> registeredSubtypes) {
        settOppForTypegenereringFrontend(new FellesTypeNameResolver(), registeredSubtypes);
    }

    public static void settOppForTypegenereringFrontend(TypeNameResolver typeNameResolver, Set<Class<?>> registeredSubtypes) {
        // Påfølgende ModelConverts oppsett er tilpasset fra K9 sin openapi-spec-utils: https://github.com/navikt/openapi-spec-utils

        // Denne gjør at enums trekkes ut som egne typer istedenfor inline
        ModelResolver.enumsAsRef = true;
        ModelConverters.reset();

        // Her ber vi den om å inkludere pakkenavn i typenavnet. Da risikerer vi ikke kollisjon ved like typenavn. fqn = fully-qualified-name.
        // Ved kollisjon vil den som er sist prosessert overskrive alle tidligere.
        typeNameResolver.setUseFqn(true);

        ModelConverters.getInstance().addConverter(new ModelResolver(lagObjectMapperUtenJsonSubTypeAnnotasjoner(),  typeNameResolver));

        if (!registeredSubtypes.isEmpty()) {
            ModelConverters.getInstance().addConverter(new RegisteredSubtypesModelConverter(registeredSubtypes));
        }
        ModelConverters.getInstance().addConverter(new JsonSubTypesModelConverter());
        ModelConverters.getInstance().addConverter(new DiscriminatorModelConverter(new RefToClassLookup()));
        ModelConverters.getInstance().addConverter(new EnumVarnamesConverter());
    }

    private static JsonMapper lagObjectMapperUtenJsonSubTypeAnnotasjoner() {
        return DefaultJsonMapper.getJsonMapper().rebuild()
            // OpenApi-spec som blir generert er ikke alltid konsekvent på rekkefølgen til properties.
            // Ved å skru på disse flaggene blir output deterministic og det blir enklere å se hva som faktisk er diff fra forrige typegenerering
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            // Unngå at records sorteres as-is i feltrekkefølge.
            //.disable(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST)
            // Inkluder for å unngå å få med Ctor som er basert på noe helt annet enn det som tas med i Dto (fx basert på domeneobjekt)
            //.changeDefaultVisibility(v -> v
              //  .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
                //.withScalarConstructorVisibility(JsonAutoDetect.Visibility.NONE))
            // Fjern alle annotasjoner om JsonSubTypes. Hvis disse er med i generasjon av openapi spec får vi sirkulære avhengigheter.
            // Det skjer ved at superklassen sier den har "oneOf" arvingene sine. Mens en arving sier den har "allOf" forelderen sin.
            // Ved å fjerne jsonSubType annotasjoner får vi heller en enveis-lenke der superklassen definerer arvingene sine med "oneOf".
            .annotationIntrospector(new NoJsonSubTypesAnnotationIntrospector())
            .build();
    }
}
