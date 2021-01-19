package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toSet;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.toJson;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.connectionManager;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createKeepAliveStrategy;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.defaultHeaders;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.defaultRequestConfig;
import static org.apache.commons.lang3.ArrayUtils.addFirst;
import static org.apache.commons.lang3.reflect.ConstructorUtils.invokeConstructor;
import static org.glassfish.jersey.apache.connector.ApacheConnectorProvider.getHttpClient;
import static org.glassfish.jersey.client.ClientProperties.PROXY_URI;
import static org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache.connector.ApacheHttpClientBuilderConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper;
import no.nav.vedtak.felles.integrasjon.rest.HttpRequestRetryHandler;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler.StringResponseHandler;

/**
 *
 * Denne klassen er en felles superklasse for alle REST-klienter, uavhengig av
 * hvordan de propagerer sikkerhetsheadere. Den tilgjengeliggjør en Jersey
 * {@link Client} konfigurert på følgende måte:
 * <ol>
 * <li>Underliggende transport provider er {@link HttpClient} Denne konfigureres
 * via {@link .RestClientSupportProdusent} slik at den har de samme egenskaper
 * som tidligere</li>
 * <li>Proxy er satt om subklasser konstrueres med en proxy-URL</li>
 * <li>{@link StandardHeadersRequestInterceptor} registreres, denne sørger for
 * propagering av callID (med et par navnevariasjoner)
 * <li>Jackson mapping rgistreres. Det er mulig å sende inn sin egen mapper, om
 * det ikke gjøres brukes {@link DefaultJsonMapper}
 * <li>Øvrige filtere registres</li>
 * </ol>
 * Ved klassebasert konfigurasjon av filtere må disse ha en no-args constructor.
 * Patch operasjoner gjøres ved å falle ned på den underliggende
 * transport-provideren.
 */
public abstract class AbstractJerseyRestClient {

    public static final String TEMA = "TEMA";
    public static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    public static final String DEFAULT_NAV_CONSUMERID = "Nav-Consumer-Id";
    public static final String NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token";
    public static final String DEFAULT_NAV_CALLID = "Nav-Callid";
    public static final String ALT_NAV_CALL_ID = "nav-call-id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJerseyRestClient.class);
    protected final Client client;
    private final ObjectMapper objectMapper;

    AbstractJerseyRestClient() {
        this(null, null, Set.of());
    }

    AbstractJerseyRestClient(ClientRequestFilter... filters) {
        this(null, null, filters);
    }

    AbstractJerseyRestClient(Class<? extends ClientRequestFilter>... filterClasses) {
        this(mapper, null, filterClasses);
    }

    AbstractJerseyRestClient(ObjectMapper mapper, Class<? extends ClientRequestFilter>... filterClasses) {
        this(mapper, null, filterClasses);
    }

    AbstractJerseyRestClient(ObjectMapper mapper, URI proxy, Class<? extends ClientRequestFilter>... filterClasses) {
        this(mapper, proxy, construct(filterClasses));
    }

    AbstractJerseyRestClient(URI proxy, ClientRequestFilter... filters) {
        this(mapper, proxy, filters);

    }

    AbstractJerseyRestClient(ObjectMapper mapper, ClientRequestFilter... filters) {
        this(mapper, null, filters);
    }

    AbstractJerseyRestClient(ObjectMapper mapper, URI proxy, ClientRequestFilter... filters) {
        this(mapper, proxy, Set.of(filters));
    }

    private AbstractJerseyRestClient(ObjectMapper mapper, URI proxy, Set<? extends ClientRequestFilter> filters) {
        var cfg = new ClientConfig();
        Optional.ofNullable(proxy)
                .ifPresent(p -> cfg.property(PROXY_URI, p));
        cfg.register(jacksonProvider(mapper));
        cfg.connectorProvider(new ApacheConnectorProvider());
        cfg.register((ApacheHttpClientBuilderConfigurator) (b) -> {
            return b.setDefaultHeaders(defaultHeaders())
                    .addInterceptorFirst(new StandardHeadersRequestInterceptor())
                    .setKeepAliveStrategy(createKeepAliveStrategy(30))
                    .setDefaultRequestConfig(defaultRequestConfig())
                    .setRetryHandler(new HttpRequestRetryHandler())
                    .setConnectionManager(connectionManager());
        });
        filters.stream().forEach(f -> {
            LOG.info("Registrer filter {}", f.getClass());
            cfg.register(f);
        });
        this.objectMapper = mapper;
        client = ClientBuilder.newClient(cfg);
    }

    private static JacksonJaxbJsonProvider jacksonProvider(ObjectMapper mapper) {
        return Optional.ofNullable(mapper)
                .map(m -> new JacksonJaxbJsonProvider(m, DEFAULT_ANNOTATIONS))
                .orElse(new JacksonJaxbJsonProvider());
    }

    private static Set<ClientRequestFilter> construct(Class<? extends ClientRequestFilter>... filters) {
        return Arrays.stream(filters)
                .map(AbstractJerseyRestClient::construct)
                .collect(toSet());
    }

    protected static ClientRequestFilter construct(Class<? extends ClientRequestFilter> clazz) {
        try {
            return invokeConstructor(clazz);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected String patch(URI endpoint, Object obj, Header... headers) {
        var entity = new HttpPatch(endpoint);
        entity.setEntity(new StringEntity(toJson(obj), UTF_8));
        try {
            Arrays.stream(headers).forEach(entity::addHeader);
            return getHttpClient(client).execute(entity, new StringResponseHandler(entity.getURI()));
        } catch (IOException e) {
            throw new TekniskException("F-432937", entity.getURI(), e);
        }
    }

    protected static <T extends ClientRequestFilter> T[] addIfRequiredNotPresent(T[] filters, final T required) {
        LOG.info("Sjekker påkrevd filter {} mot {}", required, Arrays.toString(filters));
        var alle = Arrays.stream(filters)
                .filter(f -> required.getClass().isAssignableFrom(f.getClass()))
                .findFirst()
                .map(m -> filters)
                .orElseGet(() -> logAndAdd(filters, required));
        LOG.info("Filtere for klient er {}", Arrays.toString(alle));
        return alle;
    }

    private static <T> T[] logAndAdd(final T[] filters, final T required) {
        LOG.info("Legger til påkrevd filter {}", required.getClass());
        return addFirst(filters, required);
    }

    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
