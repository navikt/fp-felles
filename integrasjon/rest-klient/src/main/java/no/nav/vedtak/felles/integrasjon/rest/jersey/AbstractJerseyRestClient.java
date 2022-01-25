package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.Level.FINE;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.connectionManager;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createKeepAliveStrategy;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.requestConfig;
import static no.nav.vedtak.log.metrics.MetricsUtil.utvidMedHistogram;
import static no.nav.vedtak.mapper.json.DefaultJsonMapper.toJson;
import static org.glassfish.jersey.apache.connector.ApacheConnectorProvider.getHttpClient;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.REQUEST_ENTITY_PROCESSING;
import static org.glassfish.jersey.client.RequestEntityProcessing.BUFFERED;
import static org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS;
import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.PAYLOAD_ANY;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.LogManager;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache.connector.ApacheHttpClientBuilderConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.httpcomponents.MicrometerHttpRequestExecutor;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.HttpRequestRetryHandler;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler.StringResponseHandler;
import no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

/**
 *
 * Denne klassen er en felles superklasse for alle REST-klienter, uavhengig av
 * hvordan de propagerer sikkerhetsheadere. Den tilgjengeliggjør en Jersey
 * {@link Client} konfigurert på følgende måte:
 * <ol>
 * <li>Underliggende transport provider er {@link HttpClient} Denne konfigureres
 * via {@link RestClientSupportProdusent} slik at den har de samme egenskaper
 * som tidligere</li>
 * <li>Proxy er satt om subklasser konstrueres med en proxy-URL</li>
 * <li>{@link StandardHeadersRequestInterceptor} registreres, denne sørger for
 * propagering av callID (med et par navnevariasjoner)
 * <li>Jackson mapping registreres.</li>
 * <li>Øvrige filtere registres</li>
 * </ol>
 * Patch operasjoner gjøres ved å falle gjennom til den underliggende
 * transport-provideren.
 */
public abstract class AbstractJerseyRestClient {

    private static final String HTTPCOMPONENTS_HTTPCLIENT_REQUEST = "httpcomponents.httpclient.request";
    private static final Environment ENV = Environment.current();

    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    public static final String TEMA = "TEMA";
    public static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    public static final String DEFAULT_NAV_CONSUMERID = "Nav-Consumer-Id";
    public static final String NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token";
    public static final String DEFAULT_NAV_CALLID = "Nav-Callid";
    public static final String ALT_NAV_CALL_ID = "nav-call-id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = (int) SECONDS.toMillis(30);
    private static final int DEFAULT_READ_TIMEOUT_MS = (int) SECONDS.toMillis(10);

    protected final Client client;
    protected final Client asyncClient;
    private final Invoker invoker;

    AbstractJerseyRestClient() {
        this(null, Set.of());
    }

    protected AbstractJerseyRestClient(ClientRequestFilter... filters) {
        this(null, filters);
    }

    AbstractJerseyRestClient(Invoker invoker, ClientRequestFilter... filters) {
        this(invoker, Set.of(filters));
    }

    private AbstractJerseyRestClient(Invoker invoker, Set<? extends ClientRequestFilter> filters) {

        var cfg = new ClientConfig();
        cfg.register(jacksonProvider(DefaultJsonMapper.getObjectMapper()));
        cfg.connectorProvider(new ApacheConnectorProvider());
        cfg.register((ApacheHttpClientBuilderConfigurator) (b) -> {
            return b.addInterceptorFirst(new StandardHeadersRequestInterceptor())
                    .setKeepAliveStrategy(createKeepAliveStrategy(30))
                    .setDefaultRequestConfig(requestConfig())
                    .setRetryHandler(new HttpRequestRetryHandler())
                    .setRequestExecutor(MicrometerHttpRequestExecutor
                            .builder(globalRegistry)
                            .exportTagsForRoute(true)
                            .uriMapper(new JerseyUriMapper())
                            .tags(List.of(Tag.of("client", getClass().getSimpleName())))
                            .build())
                    .setConnectionManager(connectionManager());
        });
        utvidMedHistogram(HTTPCOMPONENTS_HTTPCLIENT_REQUEST);
        filters.stream().forEach(cfg::register);
        cfg//.register(ErrorResponseHandlingClientResponseFilter.class)
                .register(new HeaderLoggingFilter())
                .register(new LoggingFeature(java.util.logging.Logger.getLogger(getClass().getName()),
                        FINE, PAYLOAD_ANY, 10000));

        this.client = ClientBuilder.newClient(cfg)
                .property(CONNECT_TIMEOUT, ENV.getProperty(CONNECT_TIMEOUT, int.class, DEFAULT_CONNECT_TIMEOUT_MS))
                .property(READ_TIMEOUT, ENV.getProperty(READ_TIMEOUT, int.class, DEFAULT_READ_TIMEOUT_MS))
                .property(REQUEST_ENTITY_PROCESSING, BUFFERED);

        this.asyncClient = client.register(PropagatingThreadPoolExecutorProvider.class);
        this.invoker = invoker != null ? invoker : new ExceptionTranslatingInvoker();
    }

    private static JacksonJsonProvider jacksonProvider(ObjectMapper mapper) {
        return Optional.ofNullable(mapper)
                .map(m -> new JacksonJaxbJsonProvider(m, DEFAULT_ANNOTATIONS))
                .orElseGet(() -> new JacksonJaxbJsonProvider());
    }

    public <T> T invoke(Invocation i, Class<T> clazz) {
        return invoker.invoke(i, clazz);
    }

    public void invoke(Invocation i) {
        invoker.invoke(i);
    }

    public <T> T invoke(Invocation i, GenericType<T> type) {
        return invoker.invoke(i, type);
    }

    protected String patch(URI endpoint, Object obj, Header... headers) {
        var entity = new HttpPatch(endpoint);
        entity.setEntity(new StringEntity(toJson(obj), UTF_8));
        try {
            Arrays.stream(headers)
                    .forEach(entity::addHeader);
            return getHttpClient(client).execute(entity, new StringResponseHandler(entity.getURI()));
        } catch (IOException e) {
            throw new TekniskException("F-432937", String.format("Kunne ikke patche %s", entity.getURI()), e);
        }
    }
}
