package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.toJson;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.connectionManager;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createKeepAliveStrategy;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.defaultHeaders;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.defaultRequestConfig;
import static org.apache.commons.lang3.reflect.ConstructorUtils.invokeConstructor;
import static org.glassfish.jersey.apache.connector.ApacheConnectorProvider.getHttpClient;
import static org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
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
import no.nav.vedtak.felles.integrasjon.rest.HttpRequestRetryHandler;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler.StringResponseHandler;

abstract class AbstractJerseyRestClient {

    static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    static final String DEFAULT_NAV_CONSUMERID = "Nav-Consumer-Id";
    static final String DEFAULT_NAV_CALLID = "Nav-Callid";
    static final String ALT_NAV_CALL_ID = "nav-call-id";
    static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    protected final Client client;

    AbstractJerseyRestClient(Class<? extends ClientRequestFilter>... filters) throws Exception {
        this(mapper, filters);
    }

    AbstractJerseyRestClient(ObjectMapper mapper, Class<? extends ClientRequestFilter>... filters) {
        this(mapper, construct(filters));
    }

    AbstractJerseyRestClient(ClientRequestFilter... filters) {
        this(mapper, filters);
    }

    AbstractJerseyRestClient(ObjectMapper mapper, ClientRequestFilter... filters) {
        this(mapper, asList(filters));
    }

    private AbstractJerseyRestClient(ObjectMapper mapper, List<? extends ClientRequestFilter> filters) {
        var cfg = new ClientConfig();
        cfg.register(new JacksonJaxbJsonProvider(mapper, DEFAULT_ANNOTATIONS));
        cfg.connectorProvider(new ApacheConnectorProvider());
        cfg.register((ApacheHttpClientBuilderConfigurator) (b) -> {
            return b.setDefaultHeaders(defaultHeaders())
                    .setKeepAliveStrategy(createKeepAliveStrategy(30))
                    .setDefaultRequestConfig(defaultRequestConfig())
                    .setRetryHandler(new HttpRequestRetryHandler())
                    .setConnectionManager(connectionManager());
        });
        filters.stream().forEach(cfg::register);
        client = ClientBuilder.newClient(cfg);

    }

    private static List<ClientRequestFilter> construct(Class<? extends ClientRequestFilter>... filters) {
        return Arrays.stream(filters)
                .map(AbstractJerseyRestClient::construct)
                .collect(toList());
    }

    private static ClientRequestFilter construct(Class<? extends ClientRequestFilter> clazz) {
        try {
            return invokeConstructor(clazz);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String patch(URI endpoint, Object obj, Set<Header> headers) {
        try {
            var patch = new HttpPatch(endpoint);
            patch.setEntity(new StringEntity(toJson(obj), UTF_8));
            headers.forEach(patch::addHeader);
            return getHttpClient(client).execute(patch, new StringResponseHandler(endpoint));
        } catch (IOException e) {
            throw new TekniskException("F-432937", endpoint, e);
        }
    }

    protected static ObjectMapper getObjectMapper() {
        return mapper;
    }
}
