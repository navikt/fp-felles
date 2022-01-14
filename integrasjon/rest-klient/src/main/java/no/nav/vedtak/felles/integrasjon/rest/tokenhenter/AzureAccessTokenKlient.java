package no.nav.vedtak.felles.integrasjon.rest.tokenhenter;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.foreldrepenger.felles.integrasjon.rest.DefaultJsonMapper;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.oidc.OidcProvider;
import no.nav.vedtak.sikkerhet.oidc.OidcProviderConfig;
import no.nav.vedtak.sikkerhet.oidc.OidcProviderType;


public class AzureAccessTokenKlient {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(AzureAccessTokenKlient.class);
    private static final ObjectReader READER = DefaultJsonMapper.getObjectMapper().readerFor(OidcTokenResponse.class);

    private final URI tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final String azureProxy;

    private static final int EXPIRE_IN_SECONDS = 15 * 60;

    private String accessToken;
    private Instant accessTokenExpiryTime;

    public AzureAccessTokenKlient() {
        tokenEndpoint = OidcProviderConfig.instance().getOidcProvider(OidcProviderType.AZUREAD).map(OidcProvider::getTokenEndpoint).orElseThrow();
        clientId = ENV.getProperty("azure.app.client.id", "foreldrepenger");
        clientSecret = ENV.getProperty("azure.app.client.secret", "foreldrepenger");
        azureProxy = ENV.getProperty("azure.http.proxy","http://webproxy.nais:8088");
    }

    public synchronized String hentAccessToken(String scope) {
        if (accessToken != null && accessTokenExpiryTime.isAfter(Instant.now())) {
            return accessToken;
        }
        var token = hentToken(clientId,
            clientSecret,
            tokenEndpoint,
            azureProxy,
            scope);
        LOG.info("AzureAD hentet token for scope {} fikk token av type {} utløper {}", scope, token.token_type(), token.expires_in());
        accessToken = token.access_token();
        accessTokenExpiryTime = Instant.now().plusSeconds(EXPIRE_IN_SECONDS);
        return accessToken;
    }

    private static OidcTokenResponse hentToken(String clientId, String clientSecret, URI tokenEndpoint, String proxy, String scope) {
        var entity = requestEntity(clientId, clientSecret, scope);
        var httpPost = httpPost(entity, tokenEndpoint, proxy);
        String responseEntity;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            responseEntity = client.execute(httpPost, new BasicResponseHandler());
        } catch (IOException e) {
            throw new TekniskException("F-432937", String.format("IOException ved kommunikasjon med server [%s]", tokenEndpoint), e);
        }
        try {
            return READER.readValue(responseEntity);
        } catch (JsonProcessingException e) {
            throw new TekniskException("F-208314", "Kunne ikke serialisere objekt til JSON", e);
        }
    }

    private static String requestEntity(String clientId, String clientSecret, String scope) {
        return "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret + "&scope=" + scope;
    }

    private static HttpPost httpPost(String entity, URI tokenEndpoint, String tokenEndpointProxy) {
        HttpPost post = new HttpPost(tokenEndpoint);
        post.setEntity(new StringEntity(entity, StandardCharsets.UTF_8));
        post.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());
        if (tokenEndpointProxy != null) {
            HttpHost httpHostProxy = HttpHost.create(tokenEndpointProxy);
            var requestConfig = RequestConfig.custom()
                .setProxy(httpHostProxy)
                .build();
            post.setConfig(requestConfig);
        }
        return post;
    }
}