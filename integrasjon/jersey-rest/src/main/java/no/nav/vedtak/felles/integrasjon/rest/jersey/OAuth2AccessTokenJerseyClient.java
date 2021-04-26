package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.GenericType;

public class OAuth2AccessTokenJerseyClient extends AbstractJerseyRestClient implements AccessTokenProvider {

    private final URI tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final Set<String> scopes;

    OAuth2AccessTokenJerseyClient(
            URI tokenEndpoint,
            String clientId,
            String clientSecret,
            Set<String> scopes) {
        super();
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scopes = scopes;
    }

    @Override
    public String accessToken() {
        return client.target(tokenEndpoint)
                .request(APPLICATION_JSON_TYPE)
                .post(entity(entityFra(scopes), APPLICATION_FORM_URLENCODED_TYPE), new GenericType<Map<String, String>>() {
                }).get("access_token");
    }

    private String entityFra(Set<String> scopes) {
        return "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret + "&scope=" + String.join(" ", scopes);
    }

}
