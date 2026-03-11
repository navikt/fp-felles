package no.nav.vedtak.openapi;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Application;

import io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.vedtak.exception.TekniskException;

public class OpenApiUtils {

    private final SwaggerConfiguration swaggerConfiguration;
    private final Application application;

    private OpenApiUtils(SwaggerConfiguration swaggerConfiguration, Application application) {
        this.swaggerConfiguration = swaggerConfiguration;
        this.application = application;
    }

    /*
     * Oppsett av minimal OpenApi/Swagger for en enkelt Jakarta RS Application - uten konflikter.
     * Default og håndterer også tilfelle der OpenApiresource.config er en ServletConfig i stedet for Application-klasse
     */
    public static void setupSingleApplicationOpenApi(String tittel, String contextPath, Collection<Class<?>> resourceClasses) {
        Objects.requireNonNull(tittel, "tittel");
        Objects.requireNonNull(contextPath, "contextPath");
        var info = new Info().title(tittel).version("1.0");
        var oas = openApiFrom(info, contextPath);
        var swaggerConfiguration = swaggerConfigurationFrom(oas)
            .resourceClasses(resourceClasses.stream().map(Class::getName).collect(Collectors.toSet()));
        try {
            new JaxrsOpenApiContextBuilder<>().openApiConfiguration(swaggerConfiguration).buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
    }

    /*
     * Oppsett av OpenApi/Swagger for navngitt(e) Jakarta RS Application. Unngår konflikter ved ctxId.
     */
    public static void setupOpenApi(String tittel, String contextPath,
                                    Collection<Class<?>> resourceClasses, Application application) {
        Objects.requireNonNull(tittel, "tittel");
        Objects.requireNonNull(contextPath, "contextPath");
        var info = new Info().title(tittel).version("1.0");
        openApiConfigFor(info, contextPath, application)
            .registerClasses(resourceClasses)
            .buildOpenApiContext();
    }

    public static OpenApiUtils openApiConfigFor(Info info, String contextPath, Application application) {
        var oas = openApiFrom(info, contextPath);
        var swaggerConfiguration = swaggerConfigurationFrom(oas).id(idFra(application));
        return new OpenApiUtils(swaggerConfiguration, application);
    }

    public OpenApiUtils registerClasses(Collection<Class<?>> resourceClasses) {
        swaggerConfiguration.resourceClasses(resourceClasses.stream().map(Class::getName).collect(Collectors.toSet()));
        return this;
    }

    public void buildOpenApiContext() {
        try {
            new JaxrsOpenApiContextBuilder<>()
                .ctxId(idFra(application))
                .application(application)
                .openApiConfiguration(swaggerConfiguration)
                .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
    }

    private static OpenAPI openApiFrom(Info info, String contextPath) {
        return new OpenAPI().openapi("3.1.1").info(info).addServersItem(new Server().url(contextPath));
    }

    private static SwaggerConfiguration swaggerConfigurationFrom(OpenAPI openAPI) {
        return new SwaggerConfiguration().openAPI(openAPI).prettyPrint(true).scannerClass(JaxrsAnnotationScanner.class.getName());
    }

    private static String idFra(Application application) {
        return "openapi.context.id.servlet." + application.getClass().getName();
    }
}
