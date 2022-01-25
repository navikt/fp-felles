package no.nav.vedtak.sikkerhet.oidc.config.impl;

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

import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

public class WellKnownConfigurationHelper {

    private static final Logger LOG = LoggerFactory.getLogger(WellKnownConfigurationHelper.class);
    private static final Environment ENV = Environment.current();
    private static final ObjectReader READER = DefaultJsonMapper.getObjectMapper().readerFor(WellKnownOpenIdConfiguration.class);
    private static final String STANDARD_WELL_KNOWN_PATH = ".well-known/openid-configuration";

    private static Map<String, WellKnownOpenIdConfiguration> wellKnownConfigMap = Collections.synchronizedMap(new LinkedHashMap<>());

    public static WellKnownOpenIdConfiguration getWellKnownConfig(URI wellKnownUrl) {
        return getWellKnownConfig(wellKnownUrl.toString(), null);
    }

    static synchronized WellKnownOpenIdConfiguration getWellKnownConfig(String wellKnownUrl, URI proxyUrl) {
        if (wellKnownConfigMap.get(wellKnownUrl) == null) {
            wellKnownConfigMap.put(wellKnownUrl, hentWellKnownConfig(wellKnownUrl, proxyUrl));
        }
        return wellKnownConfigMap.get(wellKnownUrl);
    }

    static Optional<String> getIssuerFra(String wellKnownURL) {
        return getIssuerFra(wellKnownURL, null);
    }

    static Optional<String> getIssuerFra(String wellKnownURL, URI proxyUrl) {
        return Optional.ofNullable(wellKnownURL).map(u -> getWellKnownConfig(u, proxyUrl).issuer());
    }

    static Optional<String> getJwksFra(String wellKnownURL) {
        return getJwksFra(wellKnownURL, null);
    }

    static Optional<String> getJwksFra(String wellKnownURL, URI proxyUrl) {
        return Optional.ofNullable(wellKnownURL).map(u -> getWellKnownConfig(u, proxyUrl).jwks_uri());
    }

    static Optional<String> getTokenEndpointFra(String wellKnownURL) {
        return getTokenEndpointFra(wellKnownURL, null);
    }

    static Optional<String> getTokenEndpointFra(String wellKnownURL, URI proxyUrl) {
        return Optional.ofNullable(wellKnownURL).map(u -> getWellKnownConfig(u, proxyUrl).token_endpoint());
    }

    static Optional<String> getAuthorizationEndpointFra(String wellKnownURL) {
        return Optional.ofNullable(wellKnownURL).map(u -> getWellKnownConfig(u, null).authorization_endpoint());
    }

    private static WellKnownOpenIdConfiguration hentWellKnownConfig(String wellKnownURL, URI proxyUrl) {
        try {
            if (wellKnownURL == null) return null;
            if (!wellKnownURL.toLowerCase().contains(STANDARD_WELL_KNOWN_PATH)) {
                // TODO: øk til warn eller prøv å legge på / standard path med
                LOG.info("WELLKNOWN OPENID-CONFIGURATION url uten standard suffix {}", wellKnownURL);
            }
            var clientBuilder = HttpClient.newBuilder();
            Optional.ofNullable(proxyUrl)
                .map(u -> new InetSocketAddress(u.getHost(), u.getPort()))
                .map(ProxySelector::of)
                .ifPresent(clientBuilder::proxy);
            var client = clientBuilder.build();
            var request = HttpRequest.newBuilder()
                .uri(URI.create(wellKnownURL))
                .header("accept", "application/json")
                .GET()
                .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return response != null ? READER.readValue(response) : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TekniskException("F-999999", String.format("Exception when retrieving metadata from issuer %s", wellKnownURL), e);
        } catch (IOException e) {
            throw new TekniskException("F-999999", String.format("Exception when retrieving metadata from issuer %s", wellKnownURL), e);
        }
    }

    public static void setWellKnownConfig(String wellKnownUrl, String jsonAsString) {
        guardForTestOnly();
        wellKnownConfigMap.computeIfAbsent(wellKnownUrl, key -> {
            try {
                return READER.readValue(jsonAsString);
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
