package no.nav.vedtak.sikkerhet.jwks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;

public class JwksKeyHandlerImpl implements JwksKeyHandler {
    private static final Environment ENV = Environment.current();

    public static final String PROXY_KEY = "proxy.url";

    private static final Logger LOG = LoggerFactory.getLogger(JwksKeyHandlerImpl.class);
    private static final String DEFAULT_PROXY_URL = "http://webproxy.nais:8088";
    private static final RequestConfig PROXY_CONFIG = createProxyConfig();

    private final Supplier<String> jwksStringSupplier;
    private final URL url;

    private JsonWebKeySet keyCache;

    public JwksKeyHandlerImpl(URL url, boolean useProxyForJwks) {
        this(() -> httpGet(url, useProxyForJwks), url);
    }

    public JwksKeyHandlerImpl(Supplier<String> jwksStringSupplier, String url) throws MalformedURLException {
        this(jwksStringSupplier, new URL(url));
    }

    public JwksKeyHandlerImpl(Supplier<String> jwksStringSupplier, URL url) {
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

    private static RequestConfig createProxyConfig() {
        return RequestConfig.custom()
                .setProxy(HttpHost.create(ENV.getProperty(PROXY_KEY, DEFAULT_PROXY_URL)))
                .build();
    }

    private static String httpGet(URL url, boolean useProxyForJwks) {
        if (url == null) {
            throw new TekniskException("F-836283", "Mangler konfigurasjon av jwks url");
        }
        HttpGet httpGet = new HttpGet(url.toExternalForm());
        httpGet.addHeader("accept", "application/json");
        if (useProxyForJwks) {
            httpGet.setConfig(PROXY_CONFIG);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new TekniskException("F-580666", String.format("Klarte ikke oppdatere jwks cache for %s", url), null);
                }
                return readContent(response);
            }
        } catch (IOException e) {
            throw new TekniskException("F-580666", String.format("Klarte ikke oppdatere jwks cache for %s", url), e);
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

}
