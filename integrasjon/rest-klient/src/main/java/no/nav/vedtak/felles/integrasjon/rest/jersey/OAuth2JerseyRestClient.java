package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class OAuth2JerseyRestClient extends AbstractJerseyRestClient {

    private OAuth2JerseyRestClient(
            URI tokenEndpoint,
            URI proxy,
            String clientId,
            String clientSecret,
            Set<String> scopes) {
        super(proxy, new Oauth2JerseyClientRequestFilter(new OAuth2AccessTokenJerseyClient(tokenEndpoint, clientId, clientSecret, scopes)));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String clientId;
        private String clientSecret;
        private URI tokenEndpoint;
        private URI tokenEndpointProxy;
        private Set<String> scopes;

        private Builder() {
            scopes = new HashSet<>();
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder tokenEndpoint(URI tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
            return this;
        }

        public Builder tokenEndpointProxy(URI tokenEndpointProxy) {
            this.tokenEndpointProxy = tokenEndpointProxy;
            return this;
        }

        public Builder scopes(String... scopes) {
            return this.scopes(Set.of(scopes));
        }

        public Builder scopes(Set<String> scopes) {
            this.scopes.addAll(scopes);
            return this;
        }

        public OAuth2JerseyRestClient build() {
            if (scopes.isEmpty())
                throw new IllegalArgumentException("Må settes minst et scope.");
            return new OAuth2JerseyRestClient(
                    requireNonNull(tokenEndpoint),
                    tokenEndpointProxy,
                    requireNonNull(clientId),
                    requireNonNull(clientSecret),
                    scopes);
        }
    }
}
