package no.nav.vedtak.server.jetty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.DispatcherType;
import jakarta.ws.rs.core.Application;

import org.eclipse.jetty.ee11.cdi.CdiDecoratingListener;
import org.eclipse.jetty.ee11.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.ee11.servlet.FilterHolder;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.ee11.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee11.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.GracefulHandler;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Felles oppsett av en Jetty-server for foreldrepenger-appene.
 * <p>Setter opp Server og ContextHandler m/JakartaRS-servlets, path-sikkerhet, feilhåndtering og CDI/Weld-aktivering.
 * <p>Logging-, migrering- og datasource-oppsett ligger fortsatt i den enkelte app sin bootstrap.
 * <p>Nøkkelpoeng: {@link #registerRestApp(String, Class)} registrerer både Jersey-servleten path-sikkerhet (constraint).
 * <p>OBS på withForwardedRequestCustomizer() - brukes bare ved pålitelige {@code X-Forwarded-*}-headere, typisk intern bruk
 *
 * <p>Eksempel:
 * <pre>{@code
 * var server = JettyServerBuilder.builder()
 *     .port(serverPort)
 *     .contextPath("/fpsak")
 *     .addEventListener(new ServiceStarterListener())
 *     .registerRestApp(InternalApiConfig.API_URI, InternalApiConfig.class)
 *     .registerRestApp(ApiConfig.API_URI, ApiConfig.class)
 *     .registerRestApp(ForvaltningApiConfig.API_URI, ForvaltningApiConfig.class)
 *     .build();
 * server.start();
 * server.join();
 * }</pre>
 */
public final class JettyServerBuilder {

    private static final String APPLICATION = "jakarta.ws.rs.Application";

    /** Graceful drain-vindu ved shutdown. Må være mindre enn k8s {@code terminationGracePeriodSeconds} (default 30s). */
    static final int DEFAULT_STOP_TIMEOUT_SECONDS = 15;

    private record RestApplication(String path, Class<? extends Application> appClass) {}

    private record FilterRegistration(FilterHolder holder, String pathSpec) {}

    private Server server;
    private Integer port;
    private String contextPath;
    private boolean forwardedRequestCustomizer;
    private Duration stopTimeout = Duration.ofSeconds(DEFAULT_STOP_TIMEOUT_SECONDS);
    private ErrorHandler errorHandler = new FeilDtoErrorHandler();
    private final List<RestApplication> applications = new ArrayList<>();
    private final List<FilterRegistration> filters = new ArrayList<>();
    private final List<EventListener> eventListeners = new ArrayList<>();
    private final List<String> allowedPaths = new ArrayList<>();

    private JettyServerBuilder(Server server) {
        this.server = server;
    }

    public static JettyServerBuilder builder() {
        return new JettyServerBuilder(new Server());
    }

    public static JettyServerBuilder builder(Server server) {
        if (!server.isStopped()) {
            throw new IllegalArgumentException("Server kjører allerede");
        }
        return new JettyServerBuilder(server);
    }

    /** Port serveren skal lytte på. Påkrevd. */
    public JettyServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    /** Context path for {@link ServletContextHandler}. Utelates for å kjøre på root. */
    public JettyServerBuilder contextPath(String contextPath) {
        if (contextPath != null && (!contextPath.startsWith("/") || contextPath.endsWith("/"))) {
            throw new IllegalArgumentException("Illegal contextPath (need /path): " + contextPath);
        }
        this.contextPath = contextPath;
        return this;
    }

    /** Kun trygt bak en proxy/ingress som setter/saniterer disse headerne. Ikke bruk for eksponerte apps */
    public JettyServerBuilder withForwardedRequestCustomizer() {
        this.forwardedRequestCustomizer = true;
        return this;
    }

    /** Stop-timeout i sekunder (default 15) */
    public JettyServerBuilder stopTimeout(Duration stopTimeout) {
        this.stopTimeout = Objects.requireNonNull(stopTimeout, "stopTimeout");
        return this;
    }

    /** App-spesifikk lytter, typisk en {@code ServiceStarterListener}. */
    public JettyServerBuilder addEventListener(EventListener eventListener) {
        Objects.requireNonNull(eventListener, "eventListener");
        this.eventListeners.add(eventListener);
        return this;
    }

    /**
     * Registrer en Jersey-servlet for en REST {@link Application} på {@code path + "/*"} og åpne
     * samme path i sikkerhetsoppsettet. Init-rekkefølge tildeles ut fra registreringsrekkefølgen.
     */
    public JettyServerBuilder registerRestApp(String path, Class<? extends Application> appClass) {
        if (path == null || !path.startsWith("/") || path.endsWith("/") || path.endsWith("*")) {
            throw new IllegalArgumentException("Illegal path (need /path): " + path);
        }
        this.applications.add(new RestApplication(path, appClass));
        return this;
    }

    /** Registrer et filter (f.eks. autentiseringsfilter) på {@code pathSpec} for {@code REQUEST}-dispatch. */
    public JettyServerBuilder addFilter(FilterHolder filterHolder, String pathSpec) {
        if (pathSpec == null || pathSpec.isEmpty()) {
            throw new IllegalArgumentException("Illegal path (need /path/*): " + pathSpec);
        }
        this.filters.add(new FilterRegistration(filterHolder, pathSpec));
        return this;
    }

    /** App-spesifikk lytter, typisk en {@code ServiceStarterListener}. */
    public JettyServerBuilder addAllowedPath(String path) {
        if (path == null || !path.startsWith("/") || !path.endsWith("*")) {
            throw new IllegalArgumentException("Illegal path (need /path): " + path);
        }
        this.allowedPaths.add(path);
        return this;
    }

    /** ErrorHandler som produserer annen Dto enn FeilDto */
    public JettyServerBuilder withErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
        return this;
    }

    /** Bygg en ferdig konfigurert, men ikke startet, {@link Server}. */
    public Server build() {
        Objects.requireNonNull(port, "port må settes");
        server.addConnector(createConnector(server));
        server.setHandler(gracefulHandler(createContext()));
        // Felles FeilDto-respons for feil som oppstår før/utenfor noen context (f.eks. parser-feil)
        server.setErrorHandler(errorHandler);
        // Graceful shutdown: alltid på (k8s SIGTERM ved deploy / HPA scaledown)
        server.setStopAtShutdown(true);
        server.setStopTimeout(stopTimeout.toMillis());
        return server;
    }

    private static Handler gracefulHandler(Handler handler) {
        var graceful = new GracefulHandler();
        graceful.setHandler(handler);
        return graceful;
    }

    private Connector createConnector(Server server) {
        var httpConfig = new HttpConfiguration();
        if (forwardedRequestCustomizer) {
            httpConfig.addCustomizer(new ForwardedRequestCustomizer());
        }
        var connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(port);
        httpConfig.setSendServerVersion(false);
        return connector;
    }

    private ServletContextHandler createContext() {
        var ctx = contextPath == null
            ? new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
            : new ServletContextHandler(contextPath, ServletContextHandler.NO_SESSIONS);

        // Sikkerhet
        ctx.setSecurityHandler(simpleConstraints());

        // Felles FeilDto-respons for feil i context utenfor REST-applikasjonene
        ctx.setErrorHandler(errorHandler);

        // Filtre (f.eks. autentiseringsfilter) - kjøres før servletene
        filters.forEach(filter -> ctx.addFilter(filter.holder(), filter.pathSpec(), EnumSet.of(DispatcherType.REQUEST)));

        var initOrder = 0;
        for (var application : applications) {
            registerRestAppServlet(ctx, initOrder++, application.path(), application.appClass());
        }

        // App-spesifikke lyttere, f.eks. ServiceStarterListener
        eventListeners.forEach(ctx::addEventListener);

        // Aktiver Weld + CDI
        ctx.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
        ctx.addServletContainerInitializer(new CdiServletContainerInitializer());
        ctx.addServletContainerInitializer(new org.jboss.weld.environment.servlet.EnhancedListener());

        return ctx;
    }

    private static void registerRestAppServlet(ServletContextHandler context, int initOrder, String path, Class<?> appClass) {
        var servlet = new ServletHolder(new ServletContainer());
        servlet.setName(appClass.getName());
        servlet.setInitOrder(initOrder);
        servlet.setInitParameter(APPLICATION, appClass.getName());
        context.addServlet(servlet, path + "/*");
    }

    private ConstraintSecurityHandler simpleConstraints() {
        var handler = new ConstraintSecurityHandler();
        // Slipp gjennom til autentisering i REST / auth-filter for hver registrerte applikasjon
        applications.stream()
            .map(application -> pathConstraint(Constraint.ALLOWED, application.path() + "/*"))
            .forEach(handler::addConstraintMapping);
        allowedPaths.stream()
            .map(path -> pathConstraint(Constraint.ALLOWED, path))
            .forEach(handler::addConstraintMapping);
        // Alt annet av paths og metoder forbudt - 403
        handler.addConstraintMapping(pathConstraint(Constraint.FORBIDDEN, "/*"));
        return handler;
    }

    private static ConstraintMapping pathConstraint(Constraint constraint, String path) {
        var mapping = new ConstraintMapping();
        mapping.setConstraint(constraint);
        mapping.setPathSpec(path);
        return mapping;
    }
}
