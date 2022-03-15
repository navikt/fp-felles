package no.nav.vedtak.sikkerhet.jwks;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.TekniskException;

public class JwksKeyHandlerImpl implements JwksKeyHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JwksKeyHandlerImpl.class);

    private final Supplier<String> jwksStringSupplier;
    private final URI url;

    private JsonWebKeySet keyCache;

    public JwksKeyHandlerImpl(URI url, boolean useProxyForJwks, URI proxy) {
        this(() -> nativeGet(url, useProxyForJwks, proxy), url);
    }

    public JwksKeyHandlerImpl(Supplier<String> jwksStringSupplier, URI url) {
        this.jwksStringSupplier = jwksStringSupplier;
        this.url = url;
    }

    @Override
    public synchronized Key getValidationKey(JwtHeader header) {
        Key key = getCachedKey(header);
        if (key != null) {
            return key;
        }
        refreshKeyCache();
        return getCachedKey(header);
    }

    private Key getCachedKey(JwtHeader header) {
        if (keyCache == null) {
            return null;
        }
        List<JsonWebKey> jwks = keyCache.findJsonWebKeys(header.kid(), "RSA", "sig", null);
        if (jwks.isEmpty()) {
            return null;
        }
        if (jwks.size() == 1) {
            return jwks.get(0).getKey();
        }
        Optional<JsonWebKey> jsonWebKey = jwks.stream()
                .filter(jwk -> jwk.getAlgorithm().equals(header.algorithm()))
                .findFirst();
        return jsonWebKey.map(JsonWebKey::getKey).orElse(null);
    }

    private void setKeyCache(String jwksAsString) {
        try {
            keyCache = new JsonWebKeySet(jwksAsString);
        } catch (JoseException e) {
            LOG.warn("Klarte ikke parse jwks for {}, json: {}", url, jwksAsString, e);
        }
    }

    private void refreshKeyCache() {
        keyCache = null;
        try {
            String jwksString = jwksStringSupplier.get();
            setKeyCache(jwksString);
        } catch (RuntimeException e) {
            LOG.warn("Klarte ikke oppdatere jwks cache for {}", url, e);
        }
    }

    private static String nativeGet(URI url, boolean useProxyForJwks, URI proxy) {
        if (url == null) {
            throw new TekniskException("F-836283", "Mangler konfigurasjon av jwks url");
        }
        try {
            var clientBuilder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(20));
            if (useProxyForJwks) {
                if (proxy == null) {
                    throw kunneIkkeOppdatereJwksCache(url, new IllegalArgumentException("Skal bruke proxy, men ingen verdi angitt"));
                }
                Optional.of(proxy)
                    .map(p -> new InetSocketAddress(p.getHost(), p.getPort()))
                .map(ProxySelector::of)
                .ifPresent(clientBuilder::proxy);
            }

            var client = clientBuilder.build();
            var request = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .uri(url)
                .GET()
                .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8));
            if (response == null || response.body() == null) {
                throw new TekniskException("F-157385", "Kunne ikke hente token");
            }
            LOG.info("Hentet JWKS fra {}", url);
            return response.body();
        } catch (IOException e) {
            throw kunneIkkeOppdatereJwksCache(url, e);
        }  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw kunneIkkeOppdatereJwksCache(url, e);
        }
    }

    private static TekniskException kunneIkkeOppdatereJwksCache(URI jwksUri, Exception e) {
        return new TekniskException("F-580666", String.format("Klarte ikke oppdatere jwks cache for %s", jwksUri.toString()), e);
    }

}
