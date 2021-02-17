package no.nav.vedtak.felles.integrasjon.rest;

import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createHttpClient;

import java.io.IOException;
import java.time.Duration;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;

import no.nav.vedtak.felles.integrasjon.rest.jersey.SystemConsumerJerseyStsRestClient;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.util.LRUCache;

/**
 *
 * @deprecated Erstattes av {@link SystemConsumerJerseyStsRestClient}
 *
 *             Tilpasset PDL sin konvensjon med doble tokens bruker i
 *             Authorization og systembruker i Nav-Consumer-Token
 */
@Deprecated(since = "3.0.x", forRemoval = true)
public class SystemConsumerStsRestClient extends AbstractOidcRestClient {

    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    private static final String NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token";
    private static final String CACHE_KEY = "StsRestClient";

    private final StsAccessTokenClient stsAccessTokenClient;
    private final LRUCache<String, String> cache;

    public SystemConsumerStsRestClient(StsAccessTokenConfig config) {
        super(createHttpClient());
        // Bruker default client basert på AAD-versjonen OAuth2RestClient (brukes for
        // SPokelse)
        this.stsAccessTokenClient = new StsAccessTokenClient(HttpClients.createDefault(), config);
        this.cache = new LRUCache<>(1, Duration.ofMinutes(55).toMillis());
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        request.setHeader(NAV_CONSUMER_TOKEN_HEADER, OIDC_AUTH_HEADER_PREFIX + systemUserOIDCToken());

        return super.doExecute(target, request, context);
    }

    @Override
    protected String getOIDCToken() {
        String oidcToken = SubjectHandler.getSubjectHandler().getInternSsoToken();
        if (oidcToken != null) {
            return oidcToken;
        }

        var samlToken = SubjectHandler.getSubjectHandler().getSamlToken();
        if (samlToken != null) {
            // Arv fra P2: Kalle STS for å veksle SAML til OIDC. Eller heller sanere WS som
            // tilbys
            return systemUserOIDCToken();
        }
        throw OidcRestClientFeil.klarteIkkeSkaffeOIDCToken();
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
}
