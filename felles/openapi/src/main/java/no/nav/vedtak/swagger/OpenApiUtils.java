package no.nav.vedtak.swagger;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Application;

import io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;

public class OpenApiUtils {

    private static final Environment ENV = Environment.current();

    private final SwaggerConfiguration swaggerConfiguration;
    private final Application application;

    private OpenApiUtils(SwaggerConfiguration swaggerConfiguration, Application application) {
        this.swaggerConfiguration = swaggerConfiguration;
        this.application = application;
    }

    public static void setupOpenApi(String tittel, String beskrivelse, String defaultContextPath,
                                    Collection<Class<?>> resourceClasses, Application application) {
        Objects.requireNonNull(tittel, "tittel");
        var info = new Info()
            .title(tittel)
            .version(Optional.ofNullable(ENV.imageName()).orElse("1.0"))
            .description(Optional.ofNullable(beskrivelse).orElse(tittel));
        var contextPath = ENV.getProperty("context.path", defaultContextPath);
        openApiConfigFor(info, contextPath, application)
            .registerClasses(resourceClasses)
            .buildOpenApiContext();
    }

    public static OpenApiUtils openApiConfigFor(Info info, String contextPath, Application application) {
        var oas = new OpenAPI()
            .openapi("3.1.1")
            .info(info)
            .addServersItem(new Server().url(contextPath));
        var swaggerConfiguration = new SwaggerConfiguration()
            .id(idFra(application))
            .openAPI(oas)
            .prettyPrint(true)
            .scannerClass(JaxrsAnnotationScanner.class.getName());
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

    private static String idFra(Application application) {
        return "openapi.context.id.servlet." + application.getClass().getName();
    }
}
