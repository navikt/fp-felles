package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Invocation.Builder;

import org.glassfish.jersey.client.ClientConfig;

public abstract class AbstractJerseyRestClient {
    static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";

    private final Client client;

    public AbstractJerseyRestClient(Class<? extends ClientRequestFilter>... filters) {
        var cfg = new ClientConfig();
        cfg.register(StandardHeadersRequestFilter.class);
        Arrays.stream(filters).forEach(cfg::register);
        client = ClientBuilder.newClient(cfg);
    }

    public <T> T get(URI uriTemplate, Class<T> clazz) {
        return builder(uriTemplate)
                .get(clazz);
    }

    public <T> T post(URI uriTemplate, Object entity, Class<T> clazz) {
        return builder(uriTemplate)
                .buildPost(entity(entity, APPLICATION_JSON_TYPE))
                .invoke(clazz);
    }

    private Builder builder(URI uriTemplate) {
        return client.target(uriTemplate)
                .request(APPLICATION_JSON_TYPE);
    }
}
