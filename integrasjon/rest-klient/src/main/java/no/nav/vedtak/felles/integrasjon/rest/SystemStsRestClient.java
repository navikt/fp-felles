package no.nav.vedtak.felles.integrasjon.rest;

import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createHttpClient;

import java.time.Duration;

import org.apache.http.impl.client.HttpClients;

import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenJerseyClient;
import no.nav.vedtak.util.LRUCache;

/**
 *
 * @deprecated Erstattes av {@link StsAccessTokenJerseyClient}
 *
 *             Tilpasset DKIF sin on-behalf-of med systembruker og STS
 */
@Deprecated(since = "3.0.x", forRemoval = true)
public class SystemStsRestClient extends AbstractOidcRestClient {

    private static final String CACHE_KEY = "StsRestClient";

    private final String systembruker;
    private final StsAccessTokenClient stsAccessTokenClient;
    private final LRUCache<String, String> cache;

    public SystemStsRestClient(StsAccessTokenConfig config) {
        super(createHttpClient());
        // Bruker default client basert på AAD-versjonen OAuth2RestClient (brukes for
        // SPokelse)
        this.stsAccessTokenClient = new StsAccessTokenClient(HttpClients.createDefault(), config);
        this.cache = new LRUCache<>(1, Duration.ofMinutes(55).toMillis());
        this.systembruker = config.getUsername();
    }

    @Override
    protected String getOIDCToken() {
        return systemUserOIDCToken();
    }

    private synchronized String systemUserOIDCToken() {
        var cachedAccessToken = cache.get(CACHE_KEY);
        if (cachedAccessToken != null) {
            return cachedAccessToken;
        }
        var nyttAccessToken = stsAccessTokenClient.hentAccessToken();
        cache.put(CACHE_KEY, nyttAccessToken);
        return nyttAccessToken;
    }

    @Override
    protected String getConsumerId() {
        return systembruker;
    }
}
