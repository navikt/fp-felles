package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public class StsSystemTokenKlient {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(StsSystemTokenKlient.class);
    private static final ObjectReader READER = DefaultJsonMapper.getObjectMapper().readerFor(OidcTokenResponse.class);

    private static final String SCOPE = "openid";

    private static final String CLIENT_ID = ENV.getProperty("systembruker.username");
    private static final String CLIENT_SECRET = ENV.getProperty("systembruker.password");
    private static final URI TOKEN_ENDPOINT = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.STS)
        .map(OpenIDConfiguration::tokenEndpoint).orElseThrow();

    private static OpenIDToken accessToken;

    public static synchronized OpenIDToken hentAccessToken() {
        if (accessToken != null && !accessToken.isExpired()) {
            return accessToken.copy();
        }
        var token = hentToken();
        LOG.info("STS hentet og fikk token av type {} utløper {}", token.token_type(), token.expires_in());
        accessToken = token.toOpenIDToken(OpenIDProvider.STS, SCOPE);
        return accessToken.copy();
    }

    private static OidcTokenResponse hentToken() {
        try {
            var client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();
            var request = HttpRequest.newBuilder()
                .header("Authorization", basicCredentials(CLIENT_ID, CLIENT_SECRET))
                .header("Nav-Consumer-Id", CLIENT_ID)
                .header("Nav-Call-Id", MDCOperations.getCallId())
                .header("Cache-Control", "no-cache")
                .header("Content-type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(10))
                .uri(TOKEN_ENDPOINT)
                .POST(ofFormData())
                .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8));
            if (response == null || response.body() == null) {
                throw new TekniskException("F-157385", "Kunne ikke hente STS token");
            }
            return READER.readValue(response.body());
        } catch (JsonProcessingException e) {
            throw new TekniskException("F-208314", "Kunne ikke deserialisere objekt til JSON", e);
        } catch (IOException e) {
            throw new TekniskException("F-432937", String.format("IOException ved kommunikasjon med server [%s]", TOKEN_ENDPOINT.toString()), e);
        }  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TekniskException("F-432938", "InterruptedException ved henting av STS token", e);
        }
    }

    private static HttpRequest.BodyPublisher ofFormData() {
        var formdata = "grant_type=client_credentials&scope=" + SCOPE;
        return HttpRequest.BodyPublishers.ofString(formdata, UTF_8);
    }

    private static String basicCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes(UTF_8));
    }
}
