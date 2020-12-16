package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;

import org.glassfish.jersey.client.ClientConfig;

public abstract class AbstractJerseyRestClient {
    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";

    protected abstract String getOIDCToken();

    private final Client client;

    public AbstractJerseyRestClient() {
        client = ClientBuilder.newClient(new ClientConfig()
                .register(StandardHeadersRequestFilter.class));
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
                .request(APPLICATION_JSON_TYPE)
                .header(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + getOIDCToken());
    }
}
