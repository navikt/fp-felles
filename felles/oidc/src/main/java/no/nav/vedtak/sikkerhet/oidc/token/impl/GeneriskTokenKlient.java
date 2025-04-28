package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.klient.http.ProxyProperty;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

public class GeneriskTokenKlient {

    private static final Logger LOG = LoggerFactory.getLogger(GeneriskTokenKlient.class);

    private static final ObjectReader READER = DefaultJsonMapper.getObjectMapper().readerFor(OidcTokenResponse.class);

    public static OidcTokenResponse hentTokenRetryable(HttpRequest request, URI proxy, int retries) {
        int i = retries;
        while (i-- > 0) {
            try {
                return hentToken(request, proxy);
            } catch (TekniskException e) {
                LOG.info("Feilet {}. gang ved henting av token. Prøver på nytt", retries - i, e);
            }
        }
        return hentToken(request, proxy);
    }


    private static OidcTokenResponse hentToken(HttpRequest request, URI proxy) {
        try (var client = hentEllerByggHttpClient(proxy)) { // På sikt vurder å bruke en generell klient eller å cache. De er blitt autocloseable
            var response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8));
            if (response == null || response.body() == null || !responskode2xx(response)) {
                throw new TekniskException("F-157385", "Kunne ikke hente token");
            }
            return READER.readValue(response.body());
        } catch (JsonProcessingException e) {
            throw new TekniskException("F-208314", "Kunne ikke deserialisere objekt til JSON", e);
        } catch (IOException e) {
            throw new TekniskException("F-432937", "IOException ved kommunikasjon med server", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TekniskException("F-432938", "InterruptedException ved henting av token", e);
        }
    }

    private static boolean responskode2xx(HttpResponse<String> response) {
        var status = response.statusCode();
        return status >= 200 && status < 300;
    }

    private static HttpClient hentEllerByggHttpClient(URI proxy) {
        return HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(2))
            .proxy(ProxyProperty.getProxySelector(proxy))
            .build();
    }

}
