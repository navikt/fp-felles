package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.connectionManager;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createKeepAliveStrategy;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.defaultHeaders;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.defaultRequestConfig;
import static org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache.connector.ApacheHttpClientBuilderConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper;
import no.nav.vedtak.felles.integrasjon.rest.HttpRequestRetryHandler;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler.StringResponseHandler;

abstract class AbstractJerseyRestClient {
    static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";

    protected final Client client;

    AbstractJerseyRestClient(Class<? extends ClientRequestFilter>... filters) {
        this(mapper, filters);

    }

    AbstractJerseyRestClient(ObjectMapper mapper, Class<? extends ClientRequestFilter>... filters) {
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
        Arrays.stream(filters).forEach(cfg::register);
        client = ClientBuilder.newClient(cfg);
    }

    public String patch(URI endpoint, Object dto, Set<Header> headers) {
        HttpPatch patch = new HttpPatch(endpoint);
        String json = DefaultJsonMapper.toJson(dto);
        patch.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
        headers.forEach(patch::addHeader);
        try {
            var c = CloseableHttpClient.class.cast(ApacheConnectorProvider.getHttpClient(client));
            return c.execute(patch, new StringResponseHandler(endpoint));
        } catch (IOException e) {
            throw new TekniskException("F-432937", endpoint, e);
        }
    }

    public <T> T post(URI uriTemplate, Object entity, Class<T> clazz) {
        return client.target(uriTemplate)
                .request(APPLICATION_JSON_TYPE)
                .buildPost(json(entity))
                .invoke(clazz);
    }

    protected static ObjectMapper getObjectMapper() {
        return mapper;
    }

}
