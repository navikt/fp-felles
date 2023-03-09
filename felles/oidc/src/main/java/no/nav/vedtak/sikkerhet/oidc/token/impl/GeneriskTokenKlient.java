package no.nav.vedtak.sikkerhet.oidc.token.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GeneriskTokenKlient {

    private static final ObjectReader READER = DefaultJsonMapper.getObjectMapper().readerFor(OidcTokenResponse.class);


    public static OidcTokenResponse hentToken(HttpRequest request, URI proxy) {
        try {
            var useProxySelector = Optional.ofNullable(proxy)
                .map(p -> new InetSocketAddress(p.getHost(), p.getPort()))
                .map(ProxySelector::of)
                .orElse(HttpClient.Builder.NO_PROXY);
            var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(20))
                .proxy(useProxySelector)
                .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8));
            if (response == null || response.body() == null) {
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
}
