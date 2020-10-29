package no.nav.vedtak.isso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.domene.IdTokenAndRefreshToken;
import no.nav.vedtak.sikkerhet.oidc.IdTokenAndRefreshTokenProvider;
import no.nav.vedtak.util.env.Environment;

// TODO, denne klassen er en katastrofe
public class OpenAMHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(OpenAMHelper.class);

    private static final Environment ENV = Environment.current();

    public static final String OPEN_ID_CONNECT_ISSO_HOST = "OpenIdConnect.issoHost";
    public static final String OPEN_ID_CONNECT_USERNAME = "OpenIdConnect.username";
    public static final String OPEN_ID_CONNECT_PASSWORD = "OpenIdConnect.password";

    private static final String OAUTH2_ENDPOINT = "/oauth2";
    private static final String JSON_AUTH_ENDPOINT = "/json/authenticate";
    private static final String WELL_KNOWN_ENDPOINT = "/.well-known/openid-configuration";
    public static final String ISSUER_KEY = "issuer";
    public static final String JWKS_URI_KEY = "jwks_uri";
    public static final String AUTHORIZATION_ENDPOINT_KEY = "authorization_endpoint";

    private String redirectUriEncoded;

    private static JsonNode wellKnownConfig;

    public OpenAMHelper() {
        try {
            redirectUriEncoded = URLEncoder.encode(ServerInfo.instance().getCallbackUrl(),
                    StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw OpenAmFeil.FACTORY.feilIKonfigurertRedirectUri(ServerInfo.instance().getCallbackUrl(), e)
                    .toException();
        }
    }

    public static String getIssoHostUrl() {
        return ENV.getProperty(OPEN_ID_CONNECT_ISSO_HOST);
    }

    public static String getIssoUserName() {
        return ENV.getProperty(OPEN_ID_CONNECT_USERNAME);
    }

    public static String getIssoPassword() {
        return ENV.getProperty(OPEN_ID_CONNECT_PASSWORD);
    }

    public static String getIssoIssuerUrl() {
        return getStringFromWellKnownConfig(ISSUER_KEY);
    }

    public static String getIssoJwksUrl() {
        return getStringFromWellKnownConfig(JWKS_URI_KEY);
    }

    public static String getAuthorizationEndpoint() {
        return getStringFromWellKnownConfig(AUTHORIZATION_ENDPOINT_KEY);
    }

    public IdTokenAndRefreshToken getToken() throws IOException {
        return getToken(ENV.getProperty("systembruker.username"), ENV.getProperty("systembruker.password"));
    }

    IdTokenAndRefreshToken getToken(String brukernavn, String passord) throws IOException {
        if (brukernavn == null || passord == null || brukernavn.isEmpty() || passord.isEmpty()) {
            throw new IllegalArgumentException("Brukernavn og/eller passord mangler.");
        }
        var cookieStore = new BasicCookieStore();
        try (var httpClient = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultCookieStore(cookieStore).build()) {
            authenticateUser(httpClient, cookieStore, brukernavn, passord);
            String authorizationCode = hentAuthorizationCode(httpClient);

            return new IdTokenAndRefreshTokenProvider().getToken(authorizationCode,
                    URI.create(ServerInfo.instance().getCallbackUrl()));
        }
    }

    public static void setWellKnownConfig(String jsonAsString) {
        try {
            wellKnownConfig = OBJECT_MAPPER.reader().readTree(jsonAsString);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Ugyldig json: ", e);
        }
    }

    public static void unsetWellKnownConfig() {
        wellKnownConfig = null;
    }

    public static JsonNode getWellKnownConfig() {
        return getWellKnownConfig(getIssoHostUrl() + WELL_KNOWN_ENDPOINT);
    }

    public static JsonNode getWellKnownConfig(String url) {
        if (wellKnownConfig == null) {
            return get(url, new HttpGet(url));
        }
        return wellKnownConfig;
    }

    private static JsonNode getWellKnownConfigUncached(String url) {

        var get = new HttpGet(url);
        var proxy = ENV.getProperty("http.proxy");
        if (proxy != null) {
            get.setConfig(proxy(proxy));
        }
        return get(url, get);
    }

    private static JsonNode get(String url, HttpGet get) {
        try (var response = HttpClientBuilder.create().build().execute(get)) {
            try (InputStreamReader isr = new InputStreamReader(response.getEntity().getContent(),
                    StandardCharsets.UTF_8)) {
                try (BufferedReader br = new BufferedReader(isr)) {
                    String responseString = br.lines().collect(Collectors.joining("\n"));
                    if (response.getStatusLine().getStatusCode() == 200) {
                        return OBJECT_MAPPER.reader().readTree(responseString);
                    } else {
                        throw OpenAmFeil.FACTORY.uforventetResponsFraOpenAM(
                                response.getStatusLine().getStatusCode(), responseString).toException();
                    }
                }
            }
        } catch (IOException e) {
            throw OpenAmFeil.FACTORY.serviceDiscoveryFailed(url, e).toException();
        } finally {
            get.reset();
        }
    }

    public static String getStringFromWellKnownConfig(JsonNode node, String key) {
        return Optional.ofNullable(node)
                .map(n -> n.get(key))
                .filter(Objects::nonNull)
                .map(JsonNode::asText)
                .orElse(null);
    }

    public static String getStringFromWellKnownConfig(String key) {
        return getStringFromWellKnownConfig(getWellKnownConfig(), key);
    }

    private void authenticateUser(CloseableHttpClient httpClient, CookieStore cookieStore, String brukernavn,
            String passord) throws IOException {
        String jsonAuthUrl = getIssoHostUrl().replace(OAUTH2_ENDPOINT, JSON_AUTH_ENDPOINT);

        String template = post(httpClient, jsonAuthUrl, null, Function.identity(),
                "Authorization: Negotiate");
        String utfyltTemplate;
        try {
            EndUserAuthorizationTemplate json = OBJECT_MAPPER.readValue(template, EndUserAuthorizationTemplate.class);
            json.setBrukernavn(brukernavn);
            json.setPassord(passord);
            utfyltTemplate = OBJECT_MAPPER.writeValueAsString(json);
        } catch (IOException e) {
            throw OpenAmFeil.FACTORY.uventetFeilVedUtfyllingAvAuthorizationTemplate(e).toException();
        }

        Function<String, String> hentSessionTokenFraResult = result -> {
            return parseJSON(result).get("tokenId");
        };

        String issoCookieValue = post(httpClient, jsonAuthUrl, utfyltTemplate, hentSessionTokenFraResult);
        BasicClientCookie cookie = new BasicClientCookie("nav-isso", issoCookieValue);
        URI uri = URI.create(getIssoHostUrl());
        cookie.setDomain("." + uri.getHost());
        cookie.setPath("/");
        cookie.setSecure(true);
        cookieStore.addCookie(cookie);
    }

    private <T> T post(CloseableHttpClient httpClient, String url, String data, Function<String, T> resultTransformer,
            String... headers) throws IOException {
        return post(httpClient, url, data, 200, resultTransformer, headers);
    }

    private <T> T post(CloseableHttpClient httpClient, String url, String data, int expectedHttpCode,
            Function<String, T> resultTransformer, String... headers) throws IOException {
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-type", "application/json");
        for (String header : headers) {
            String[] h = header.split(":");
            post.setHeader(h[0], h[1]);
        }
        if (data != null) {
            post.setEntity(new StringEntity(data, "UTF-8"));
        }

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            try (InputStreamReader isr = new InputStreamReader(response.getEntity().getContent(),
                    StandardCharsets.UTF_8)) {
                try (BufferedReader br = new BufferedReader(isr)) {
                    String responseString = br.lines().collect(Collectors.joining("\n"));
                    if (response.getStatusLine().getStatusCode() == expectedHttpCode) {
                        return resultTransformer.apply(responseString);
                    } else {
                        throw OpenAmFeil.FACTORY
                                .uforventetResponsFraOpenAM(response.getStatusLine().getStatusCode(), responseString)
                                .toException();
                    }
                }
            }
        } finally {
            post.reset();
        }
    }

    private static Map<String, String> parseJSON(String response) {
        var factory = TypeFactory.defaultInstance();
        var type = factory.constructMapType(HashMap.class, String.class, String.class);
        try {
            return OBJECT_MAPPER.readValue(response, type);
        } catch (IOException e) {
            throw OpenAmFeil.FACTORY.kunneIkkeParseJson(response, e).toException();
        }
    }

    private String hentAuthorizationCode(CloseableHttpClient httpClient) throws IOException {
        String url = getAuthorizationEndpoint() + "?response_type=code&scope=openid&client_id=" + getIssoUserName()
                + "&state=dummy&redirect_uri=" + redirectUriEncoded;
        HttpGet get = new HttpGet(url);
        get.setHeader("Content-type", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            if (response.containsHeader("Location")) {
                Pattern pattern = Pattern.compile("code=([^&]*)");
                String locationHeader = response.getFirstHeader("Location").getValue();
                Matcher matcher = pattern.matcher(locationHeader);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            throw OpenAmFeil.FACTORY.kunneIkkeFinneAuthCode(response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase()).toException();
        } finally {
            get.reset();
        }
    }

    public static String getJwksFra(String discoveryURL) {
        return getWellKnownFra(discoveryURL, JWKS_URI_KEY);
    }

    public static String getIssuerFra(String discoveryURL) {
        return getWellKnownFra(discoveryURL, ISSUER_KEY);
    }

    private static String getWellKnownFra(String discoveryURL, String key) {
        try {
            return Optional.ofNullable(discoveryURL)
                    .map(OpenAMHelper::getWellKnownConfigUncached)
                    .map(c -> getStringFromWellKnownConfig(c, key))
                    .orElse(null);
        } catch (Exception e) {
            LOG.warn("OIDC oppslag av {} feilet", key, e);
            return null;
        }
    }

    private static RequestConfig proxy(String proxy) {
        return RequestConfig.custom()
                .setProxy(HttpHost.create(proxy))
                .build();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[redirectUriEncoded=" + redirectUriEncoded + "]";
    }

}
