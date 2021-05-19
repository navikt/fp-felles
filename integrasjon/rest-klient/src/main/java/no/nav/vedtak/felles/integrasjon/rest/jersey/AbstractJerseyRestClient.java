package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.FINE;
import static no.nav.foreldrepenger.felles.integrasjon.rest.DefaultJsonMapper.MAPPER;
import static no.nav.foreldrepenger.felles.integrasjon.rest.DefaultJsonMapper.toJson;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.connectionManager;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createKeepAliveStrategy;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.defaultRequestConfig;
import static no.nav.vedtak.util.env.ConfidentialMarkerFilter.CONFIDENTIAL;
import static org.glassfish.jersey.apache.connector.ApacheConnectorProvider.getHttpClient;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.PROXY_URI;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS;
import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.PAYLOAD_ANY;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.logging.LogManager;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache.connector.ApacheHttpClientBuilderConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.HttpRequestRetryHandler;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler.StringResponseHandler;
import no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent;

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

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJerseyRestClient.class);
    protected final Client client;

    AbstractJerseyRestClient() {
        this(null, Set.of());
    }

    protected AbstractJerseyRestClient(ClientRequestFilter... filters) {
        this(null, filters);
    }

    AbstractJerseyRestClient(URI proxy, ClientRequestFilter... filters) {
        this(proxy, Set.of(filters));
    }

    private AbstractJerseyRestClient(URI proxy, Set<? extends ClientRequestFilter> filters) {
        var cfg = new ClientConfig();
        Optional.ofNullable(proxy)
                .ifPresent(p -> cfg.property(PROXY_URI, p));
        cfg.register(jacksonProvider(MAPPER));

        cfg.connectorProvider(new ApacheConnectorProvider());
        cfg.register((ApacheHttpClientBuilderConfigurator) (b) -> {
            return b.addInterceptorFirst(new StandardHeadersRequestInterceptor())
                    .setKeepAliveStrategy(createKeepAliveStrategy(30))
                    .setDefaultRequestConfig(defaultRequestConfig())
                    .setRetryHandler(new HttpRequestRetryHandler())
                    .setConnectionManager(connectionManager());
        });
        filters.stream().forEach(f -> {
            LOG.info("Registrer filter {}", f.getClass());
            cfg.register(f);
        });

        cfg.register(ErrorResponseHandlingClientResponseFilter.class);
        if (ENV.isDev()) {
            cfg.register(new HeaderLoggingFilter())
                    .register(new LoggingFeature(java.util.logging.Logger.getLogger(getClass().getName()),
                            FINE, PAYLOAD_ANY, 10000));
        }
        client = ClientBuilder.newClient(cfg)
                .property(CONNECT_TIMEOUT, 10000)
                .property(READ_TIMEOUT, 30000);
        LOG.trace(CONFIDENTIAL, "Client properties {}", client.getConfiguration().getProperties());
    }

    private static JacksonJaxbJsonProvider jacksonProvider(ObjectMapper mapper) {
        return Optional.ofNullable(mapper)
                .map(m -> new JacksonJaxbJsonProvider(m, DEFAULT_ANNOTATIONS))
                .orElseGet(() -> new JacksonJaxbJsonProvider());
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
