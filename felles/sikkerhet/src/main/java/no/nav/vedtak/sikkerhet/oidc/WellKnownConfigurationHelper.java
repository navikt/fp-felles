package no.nav.vedtak.sikkerhet.oidc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.felles.integrasjon.rest.DefaultJsonMapper;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;

public class WellKnownConfigurationHelper {

    private static final Logger LOG = LoggerFactory.getLogger(WellKnownConfigurationHelper.class);
    private static final Environment ENV = Environment.current();

    private static Map<String, WellKnownOpenIdConfiguration> wellKnownConfigMap = Collections.synchronizedMap(new LinkedHashMap<>());;

    public static WellKnownOpenIdConfiguration getWellKnownConfig(URI discoveryUrl) {
        return getWellKnownConfig(discoveryUrl.toString(), null);
    }

    public static synchronized WellKnownOpenIdConfiguration getWellKnownConfig(String discoveryUrl, String proxyUrl) {
        if (wellKnownConfigMap.get(discoveryUrl) == null) {
            wellKnownConfigMap.put(discoveryUrl, hentWellKnownConfig(discoveryUrl, proxyUrl));
        }
        return wellKnownConfigMap.get(discoveryUrl);
    }

    static Optional<String> getIssuerFra(String discoveryURL) {
        return getIssuerFra(discoveryURL, null);
    }

    static Optional<String> getIssuerFra(String discoveryURL, String proxyUrl) {
        LOG.debug("Henter issuer fra {}", discoveryURL);
        return Optional.ofNullable(discoveryURL).map(u -> getWellKnownConfig(u, proxyUrl).issuer());
    }

    static Optional<String> getJwksFra(String discoveryURL) {
        return getJwksFra(discoveryURL, null);
    }

    static Optional<String> getJwksFra(String discoveryURL, String proxyUrl) {
        LOG.debug("Henter jwki_uri fra {}", discoveryURL);
        return Optional.ofNullable(discoveryURL).map(u -> getWellKnownConfig(u, proxyUrl).jwks_uri().toString());
    }

    static Optional<URI> getTokenEndpointFra(String discoveryURL) {
        return getTokenEndpointFra(discoveryURL, null);
    }

    static Optional<URI> getTokenEndpointFra(String discoveryURL, String proxyUrl) {
        LOG.debug("Henter token_endpoint fra {}", discoveryURL);
        return Optional.ofNullable(discoveryURL).map(u -> getWellKnownConfig(u, proxyUrl).token_endpoint()).map(URI::create);
    }

    static Optional<URI> getAuthorizationEndpointFra(String discoveryURL) {
        return Optional.ofNullable(discoveryURL).map(u -> getWellKnownConfig(u, null).authorization_endpoint()).map(URI::create);
    }

    private static WellKnownOpenIdConfiguration hentWellKnownConfig(String discoveryURL, String proxyUrl) {
        try {
            LOG.debug("Henter well-known konfig fra '{}'", discoveryURL);
            var clientBuilder = HttpClient.newBuilder();
            Optional.ofNullable(proxyUrl)
                .filter(s -> !s.isEmpty())
                .map(URI::create)
                .map(u -> new InetSocketAddress(u.getHost(), u.getPort()))
                .map(ProxySelector::of)
                .ifPresent(clientBuilder::proxy);
            var client = clientBuilder.build();
            var request = HttpRequest.newBuilder()
                .uri(URI.create(discoveryURL))
                .header("accept", "application/json")
                .GET()
                .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return response != null ? DefaultJsonMapper.MAPPER.readerFor(WellKnownOpenIdConfiguration.class).readValue(response) : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TekniskException("F-999999", String.format("Exception when retrieving metadata from issuer %s", discoveryURL), e);
        } catch (IOException e) {
            throw new TekniskException("F-999999", String.format("Exception when retrieving metadata from issuer %s", discoveryURL), e);
        }
    }

    public static void setWellKnownConfig(String discoveryUrl, String jsonAsString) {
        guardForTestOnly();
        wellKnownConfigMap.computeIfAbsent(discoveryUrl, key -> {
            try {
                return DefaultJsonMapper.MAPPER.readerFor(WellKnownOpenIdConfiguration.class).readValue(jsonAsString);
            } catch (IOException e) {
                throw new IllegalArgumentException("Ugyldig json: ", e);
            }
        });
    }

    public static void unsetWellKnownConfig() {
        guardForTestOnly();
        wellKnownConfigMap = new HashMap<>();
    }

    private static void guardForTestOnly() {
        if (!ENV.isLocal()) {
            throw new IllegalStateException("Skal aldri kjøres i miljø!");
        }
    }
}
