package no.nav.vedtak.felles.integrasjon.rest;

import static java.util.Objects.requireNonNull;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createHttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.impl.client.HttpClients;

import no.nav.vedtak.felles.integrasjon.rest.jersey.OAuth2JerseyRestClient;
import no.nav.vedtak.util.LRUCache;

/**
 *
 * @deprecated Erstattes av {@link OAuth2JerseyRestClient}
 *
 */
@Deprecated(since = "3.0.x", forRemoval = true)
public class OAuth2RestClient extends AbstractOidcRestClient {
    private static final String CACHE_KEY = "OAuth2RestClient";
    private final Set<String> scopes;

    private final OAuth2AccessTokenClient oAuth2AccessTokenClient;
    private final LRUCache<String, String> cache;

    private OAuth2RestClient(
            URI tokenEndpoint,
            URI tokenEndpointProxy,
            String clientId,
            String clientSecret,
            Set<String> scopes) {
        super(createHttpClient());
        // Bruker default client for tokens da client konfigurert i
        // RestClientSupportProdusent feiler mot Azure p.g.a. headere som blir satt by
        // default.
        this.oAuth2AccessTokenClient = new OAuth2AccessTokenClient(HttpClients.createDefault(), tokenEndpoint, tokenEndpointProxy, clientId,
                clientSecret);
        this.scopes = scopes;
        this.cache = new LRUCache<>(1, Duration.ofMinutes(15).toMillis());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    String getOIDCToken() {
        var cachedAccessToken = cache.get(CACHE_KEY);
        if (cachedAccessToken != null) {
            return cachedAccessToken;
        }
        var nyttAccessToken = oAuth2AccessTokenClient.hentAccessToken(scopes);
        cache.put(CACHE_KEY, nyttAccessToken);
        return nyttAccessToken;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<scopes=" + scopes + ", oAuth2AccessTokenClient=" + oAuth2AccessTokenClient.toString() + ">";
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

        public OAuth2RestClient build() {
            if (scopes.isEmpty())
                throw new IllegalArgumentException("Må settes minst et scope.");
            return new OAuth2RestClient(
                    requireNonNull(tokenEndpoint),
                    tokenEndpointProxy,
                    requireNonNull(clientId),
                    requireNonNull(clientSecret),
                    scopes);
        }
    }
}
