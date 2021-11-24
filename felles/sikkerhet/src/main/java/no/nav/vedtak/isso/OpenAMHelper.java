package no.nav.vedtak.isso;

import static no.nav.vedtak.sikkerhet.oidc.OidcProviderConfig.OPEN_AM_WELL_KNOWN_URL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.client.CookieStore;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.domene.IdTokenAndRefreshToken;
import no.nav.vedtak.sikkerhet.oidc.IdTokenAndRefreshTokenProvider;
import no.nav.vedtak.sikkerhet.oidc.OidcProviderConfig;
import no.nav.vedtak.sikkerhet.oidc.WellKnownConfigurationHelper;

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
    public static final String WELL_KNOWN_ENDPOINT = "/.well-known/openid-configuration";
    public static final String ISSUER_KEY = "issuer";
    public static final String JWKS_URI_KEY = "jwks_uri";
    public static final String AUTHORIZATION_ENDPOINT_KEY = "authorization_endpoint";

    private String redirectUriEncoded;

    public OpenAMHelper() {
        try {
            redirectUriEncoded = URLEncoder.encode(ServerInfo.instance().getCallbackUrl(),
                    StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new TekniskException("F-945077", String.format("Feil i konfigurert redirect uri: %s", ServerInfo.instance().getCallbackUrl()), e);
        }
    }

    public static String getIssoHostUrl() {
        return Optional.ofNullable(ENV.getProperty(OPEN_AM_WELL_KNOWN_URL))
            .map(url -> url.replace(WELL_KNOWN_ENDPOINT, ""))
            .orElse(ENV.getProperty(OPEN_ID_CONNECT_ISSO_HOST));
    }

    public static String getIssoUserName() {
        return Optional.ofNullable(ENV.getProperty(OidcProviderConfig.OPEN_AM_CLIENT_ID))
            .orElse(ENV.getProperty(OPEN_ID_CONNECT_USERNAME));
    }

    public static String getIssoPassword() {
        return Optional.ofNullable(ENV.getProperty(OidcProviderConfig.OPEN_AM_CLIENT_SECRET))
            .orElse(ENV.getProperty(OPEN_ID_CONNECT_PASSWORD));
    }

    public static AuthorizationServerMetadata getOpenAmWellKnownConfig() {
        return WellKnownConfigurationHelper.getWellKnownConfig
            (Optional.ofNullable(ENV.getProperty(OPEN_AM_WELL_KNOWN_URL))
                .orElse(ENV.getProperty(OPEN_ID_CONNECT_ISSO_HOST) + WELL_KNOWN_ENDPOINT));
    }

    public static String getIssoIssuerUrl() {
        return getOpenAmWellKnownConfig().getIssuer().getValue();
    }

    public static String getIssoJwksUrl() {
        return getOpenAmWellKnownConfig().getJWKSetURI().toString();
    }

    public static String getAuthorizationEndpoint() {
        return getOpenAmWellKnownConfig().getAuthorizationEndpointURI().toString();
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
            return new IdTokenAndRefreshTokenProvider().getToken(authorizationCode);
        }
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
            throw new TekniskException("F-502086", "Uventet feil ved utfylling av authorization template", e);
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
                    }
                    throw new TekniskException("F-011609",
                            String.format("Ikke-forventet respons fra OpenAm, statusCode %s og respons '%s'",
                                    response.getStatusLine().getStatusCode(), responseString));
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
            throw new TekniskException("F-404323",
                    String.format("Kunne ikke parse JSON: '%s'", response), e);
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
            throw new TekniskException("F-909480", String.format("Fant ikke auth-code på responsen, får respons: '%s - %s'",
                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
        } finally {
            get.reset();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[redirectUriEncoded=" + redirectUriEncoded + "]";
    }

}
