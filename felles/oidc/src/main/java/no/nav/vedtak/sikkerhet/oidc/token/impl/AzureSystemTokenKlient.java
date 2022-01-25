package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;


public class AzureSystemTokenKlient {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(AzureSystemTokenKlient.class);
    private static final ObjectReader READER = DefaultJsonMapper.getObjectMapper().readerFor(OidcTokenResponse.class);

    private static volatile AzureSystemTokenKlient INSTANCE; // NOSONAR

    private final URI tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final URI azureProxy;

    private final Map<String, OpenIDToken> accessToken = new LinkedHashMap<>();

    public static synchronized AzureSystemTokenKlient instance() {
        var inst = INSTANCE;
        if (inst == null) {
            inst = new AzureSystemTokenKlient();
            INSTANCE = inst;
        }
        return inst;
    }

    private AzureSystemTokenKlient() {
        var provider = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.AZUREAD).orElseThrow();
        tokenEndpoint = provider.tokenEndpoint();
        azureProxy = provider.proxy();
        clientId = provider.clientId();
        clientSecret = ENV.getProperty("azure.app.client.secret", "foreldrepenger");
    }

    public synchronized OpenIDToken hentAccessToken(String scope) {
        // Expiry normalt 3599 ...
        var heldToken = accessToken.get(scope);
        if (heldToken != null && !heldToken.isExpired()) {
            return heldToken.copy();
        }
        var token = hentAccessToken(clientId,
            clientSecret,
            tokenEndpoint,
            azureProxy,
            scope);
        LOG.info("AzureAD hentet token for scope {} fikk token av type {} utløper {}", scope, token.token_type(), token.expires_in());
        var newToken = token.toOpenIDToken(OpenIDProvider.AZUREAD);
        accessToken.put(scope, newToken);
        return newToken.copy();
    }


    private static OidcTokenResponse hentAccessToken(String clientId, String clientSecret, URI tokenEndpoint, URI proxy, String scope) {
        try {
            var clientBuilder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(20));
            Optional.ofNullable(proxy)
                .map(p -> new InetSocketAddress(p.getHost(), p.getPort()))
                .map(ProxySelector::of)
                .ifPresent(clientBuilder::proxy);
            var client = clientBuilder.build();
            var request = HttpRequest.newBuilder()
                .header("Cache-Control", "no-cache")
                .header("Content-type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(10))
                .uri(tokenEndpoint)
                .POST(ofFormData(clientId, clientSecret, scope))
                .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8));
            if (response == null || response.body() == null) {
                throw new TekniskException("F-157385", "Kunne ikke hente Azure token");
            }
            return READER.readValue(response.body());
        } catch (JsonProcessingException e) {
            throw new TekniskException("F-208314", "Kunne ikke deserialisere objekt til JSON", e);
        } catch (IOException e) {
            throw new TekniskException("F-432937", String.format("IOException ved kommunikasjon med server [%s]", tokenEndpoint.toString()), e);
        }  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TekniskException("F-432938", "InterruptedException ved henting av Azure token", e);
        }
    }

    private static HttpRequest.BodyPublisher ofFormData(String clientId, String clientSecret, String scope) {
        var formdata = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret + "&scope=" + scope;
        return HttpRequest.BodyPublishers.ofString(formdata, UTF_8);
    }

}
