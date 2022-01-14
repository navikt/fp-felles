package no.nav.vedtak.felles.integrasjon.rest;

import static java.util.Objects.requireNonNull;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createHttpClient;

import java.time.Duration;

import no.nav.vedtak.felles.integrasjon.rest.tokenhenter.AzureAccessTokenKlient;
import no.nav.vedtak.util.LRUCache;

/**
 *
 * Se også AzureJerseyRestClient}
 *
 */
public class AzureADRestClient extends AbstractOidcRestClient {

    private static final String CACHE_KEY = "AzureADRestClient";
    private final String scope;

    private final LRUCache<String, String> cache;
    private final AzureAccessTokenKlient azureAccessTokenClient;

    private AzureADRestClient(String scope) {
        super(createHttpClient());
        this.scope = scope;
        this.azureAccessTokenClient = new AzureAccessTokenKlient();
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
        var nyttAccessToken = azureAccessTokenClient.hentAccessToken(scope);
        cache.put(CACHE_KEY, nyttAccessToken);
        return nyttAccessToken;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<scope=" + scope + ">";
    }

    public static class Builder {
        private String scope;

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public AzureADRestClient build() {
            return new AzureADRestClient(requireNonNull(scope));
        }
    }
}
