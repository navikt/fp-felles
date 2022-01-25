package no.nav.vedtak.sikkerhet.jwks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
        this(() -> httpGet(url, useProxyForJwks, proxy), url);
    }

    public JwksKeyHandlerImpl(Supplier<String> jwksStringSupplier, String url) {
        this(jwksStringSupplier, URI.create(url));
    }

    private JwksKeyHandlerImpl(Supplier<String> jwksStringSupplier, URI url) {
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

    private static RequestConfig createProxyConfig(URI proxy) {
        return RequestConfig.custom()
                .setProxy(HttpHost.create(proxy.toString()))
                .build();
    }

    private static String httpGet(URI url, boolean useProxyForJwks, URI proxy) {
        if (url == null) {
            throw new TekniskException("F-836283", "Mangler konfigurasjon av jwks url");
        }
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("accept", "application/json");
        if (useProxyForJwks) {
            if (proxy == null) {
                throw kunneIkkeOppdatereJwksCache(url, new IllegalArgumentException("Skal bruke proxy, men ingen verdi angitt"));
            }
            httpGet.setConfig(createProxyConfig(proxy));
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw kunneIkkeOppdatereJwksCache(url, null);
                }
                return readContent(response);
            }
        } catch (IOException e) {
            throw kunneIkkeOppdatereJwksCache(url, e);
        } finally {
            httpGet.reset();
        }
    }

    private static String readContent(CloseableHttpResponse response) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8.name())) {
            try (BufferedReader br = new BufferedReader(isr)) {
                return br.lines().collect(Collectors.joining("\n"));
            }
        }
    }

    private static TekniskException kunneIkkeOppdatereJwksCache(URI jwksUri, Exception e) {
        return new TekniskException("F-580666", String.format("Klarte ikke oppdatere jwks cache for %s", jwksUri.toString()), e);
    }

}
