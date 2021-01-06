package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toSet;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.toJson;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.connectionManager;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createKeepAliveStrategy;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.defaultHeaders;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.defaultRequestConfig;
import static org.apache.commons.lang3.ArrayUtils.add;
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
 * <li>{@link StandardHeadersRequestFilter} registreres, denne sørger for
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

    public static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    public static final String DEFAULT_NAV_CONSUMERID = "Nav-Consumer-Id";
    public static final String DEFAULT_NAV_CALLID = "Nav-Callid";
    public static final String ALT_NAV_CALL_ID = "nav-call-id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    protected final Client client;

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
                    .setKeepAliveStrategy(createKeepAliveStrategy(30))
                    .setDefaultRequestConfig(defaultRequestConfig())
                    .setRetryHandler(new HttpRequestRetryHandler())
                    .setConnectionManager(connectionManager());
        });
        filters.stream().forEach(cfg::register);
        cfg.register(new StandardHeadersRequestFilter());
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

    protected String patch(URI endpoint, Object obj, Set<Header> headers) {
        try {
            var patch = new HttpPatch(endpoint);
            patch.setEntity(new StringEntity(toJson(obj), UTF_8));
            headers.forEach(patch::addHeader);
            return getHttpClient(client).execute(patch, new StringResponseHandler(endpoint));
        } catch (IOException e) {
            throw new TekniskException("F-432937", endpoint, e);
        }
    }

    protected static <T> T[] addIfRequiredNotPresent(final T[] filters, final T required) {
        return Arrays.stream(filters)
                .filter(e -> e.getClass().equals(required.getClass()))
                .findFirst()
                .map(m -> filters)
                .orElseGet(() -> add(filters, required));
    }

    protected static ObjectMapper getObjectMapper() {
        return mapper; // TODO hent fra client elns
    }
}
