package no.nav.vedtak.sikkerhet.jwks;

import static no.nav.vedtak.konfig.PropertyUtil.getProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import no.nav.vedtak.log.util.LoggerUtils;

public class JwksKeyHandlerImpl implements JwksKeyHandler {
    public static final String PROXY_KEY = "proxy.url";

    private static final Logger log = LoggerFactory.getLogger(JwksKeyHandlerImpl.class);
    private static final String DEFAULT_PROXY_URL = "http://webproxy.nais:8088";
    private static RequestConfig proxyConfig = createProxyConfig();

    private final Supplier<String> jwksStringSupplier;
    private URL url;

    private JsonWebKeySet keyCache;

    public JwksKeyHandlerImpl(URL url, boolean useProxyForJwks){
        this(() -> httpGet(url, useProxyForJwks));
        this.url = url;
    }

    public JwksKeyHandlerImpl(Supplier<String> jwksStringSupplier) {
        this.jwksStringSupplier = jwksStringSupplier;
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
        List<JsonWebKey> jwks = keyCache.findJsonWebKeys(header.getKid(), "RSA", "sig", null);
        if(jwks.isEmpty()){
            return null;
        }
        if(jwks.size() == 1){
            return jwks.get(0).getKey();
        }
        Optional<JsonWebKey> jsonWebKey = jwks.stream().filter(jwk -> jwk.getAlgorithm().equals(header.getAlgorithm())).findFirst();
        return jsonWebKey.map(jwk -> jwk.getKey()).orElse(null);
    }

    private void setKeyCache(String jwksAsString) {
        try {
            keyCache = new JsonWebKeySet(jwksAsString);
        } catch (JoseException e) {
            JwksFeil.FACTORY.klarteIkkeParseJWKs(url, jwksAsString, e).log(log);
        }
    }

    private void refreshKeyCache() {
        keyCache = null;
        try {
            String jwksString = jwksStringSupplier.get();
            setKeyCache(jwksString);
            // Enable ved behov log.debug("JWKs cache for {} updated with: {}", url, jwksString); //NOSONAR
        } catch (RuntimeException e) {
            JwksFeil.FACTORY.klarteIkkeOppdatereJwksCache(url, e).log(log);
        }
    }

    private static RequestConfig createProxyConfig() {
        String proxyUrl = getProperty(PROXY_KEY);
        if (proxyUrl == null) {
            proxyUrl = DEFAULT_PROXY_URL;
        }
        HttpHost proxy = HttpHost.create(proxyUrl);
        return RequestConfig.custom()
                .setProxy(proxy)
                .build();
    }

    private static String httpGet(URL url, boolean useProxyForJwks) {
        if (url == null) {
            throw JwksFeil.FACTORY.manglerKonfigurasjonAvJwksUrl().toException();
        }
        log.debug("Starting JWKS update from {}", LoggerUtils.removeLineBreaks(url.toExternalForm())); //NOSONAR
        HttpGet httpGet = new HttpGet(url.toExternalForm());
        httpGet.addHeader("accept", "application/json");
        if(useProxyForJwks){
            httpGet.setConfig(proxyConfig);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw JwksFeil.FACTORY.klarteIkkeOppdatereJwksCache(url, response.getStatusLine().getStatusCode()).toException();
                }
                return readContent(response);
            }
        } catch (IOException e) {
            throw JwksFeil.FACTORY.klarteIkkeOppdatereJwksCache(url, e).toException();
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
