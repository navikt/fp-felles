package no.nav.vedtak.isso;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.domene.IdTokenAndRefreshToken;
import no.nav.vedtak.sikkerhet.oidc.IdTokenAndRefreshTokenProvider;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

import static no.nav.vedtak.konfig.PropertyUtil.getProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OpenAMHelper {
    public static final String OPEN_ID_CONNECT_ISSO_HOST = "OpenIdConnect.issoHost";
    public static final String OPEN_ID_CONNECT_ISSO_ISSUER = "OpenIdConnect.issoIssuer";
    public static final String OPEN_ID_CONNECT_ISSO_JWKS = "OpenIdConnect.issoJwks";
    public static final String OPEN_ID_CONNECT_USERNAME = "OpenIdConnect.username";
    public static final String OPEN_ID_CONNECT_PASSWORD = "OpenIdConnect.password";

    private static final String OAUTH2_ENDPOINT = "/oauth2";
    private static final String JSON_AUTH_ENDPOINT = "/json/authenticate";

    private String redirectUriEncoded;

    public OpenAMHelper() {
        try {
            redirectUriEncoded = URLEncoder.encode(ServerInfo.instance().getCallbackUrl(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw OpenAmFeil.FACTORY.feilIKonfigurertRedirectUri(ServerInfo.instance().getCallbackUrl(), e).toException();
        }
    }

    private static Map<String, String> parseJSON(String response) {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory factory = TypeFactory.defaultInstance();
        MapType type = factory.constructMapType(HashMap.class, String.class, String.class);
        try {
            return mapper.readValue(response, type);
        } catch (IOException e) {
            throw OpenAmFeil.FACTORY.kunneIkkeParseJson(response, e).toException();
        }
    }

    public IdTokenAndRefreshToken getToken() throws IOException {
        return getToken(getProperty("systembruker.username"), getProperty("systembruker.password"));
    }

    IdTokenAndRefreshToken getToken(String brukernavn, String passord) throws IOException {
        if (brukernavn == null || passord == null || brukernavn.isEmpty() || passord.isEmpty()) {
            throw new IllegalArgumentException("Brukernavn og/eller passord mangler.");
        }
        CookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().setDefaultCookieStore(cookieStore).build()) {
            authenticateUser(httpClient, cookieStore, brukernavn, passord);
            String authorizationCode = hentAuthorizationCode(httpClient);

            return new IdTokenAndRefreshTokenProvider().getToken(authorizationCode, URI.create(ServerInfo.instance().getCallbackUrl()));
        }
    }

    private void authenticateUser(CloseableHttpClient httpClient, CookieStore cookieStore, String brukernavn, String passord) throws IOException {
        String jsonAuthUrl = getIssoHostUrl().replace(OAUTH2_ENDPOINT, JSON_AUTH_ENDPOINT);

        String template = post(httpClient, jsonAuthUrl, null, Function.identity(),
                "Authorization: Negotiate");

        ObjectMapper mapper = new ObjectMapper();
        String utfyltTemplate;
        try {
            EndUserAuthorizationTemplate json = mapper.readValue(template, EndUserAuthorizationTemplate.class);
            json.setBrukernavn(brukernavn);
            json.setPassord(passord);
            utfyltTemplate = mapper.writeValueAsString(json);
        } catch (IOException e) {
            throw OpenAmFeil.FACTORY.uventetFeilVedUtfyllingAvAuthorizationTemplate(e).toException();
        }

        Function<String, String> hentSessionTokenFraResult = result -> {
            Map<String, String> stringStringMap = parseJSON(result);
            return stringStringMap.get("tokenId");
        };

        String issoCookieValue = post(httpClient, jsonAuthUrl, utfyltTemplate, hentSessionTokenFraResult);
        BasicClientCookie cookie = new BasicClientCookie("nav-isso", issoCookieValue);
        URI uri = URI.create(getIssoHostUrl());
        cookie.setDomain("." + uri.getHost());
        cookie.setPath("/");
        cookie.setSecure(true);
        cookieStore.addCookie(cookie);
    }

    private <T> T post(CloseableHttpClient httpClient, String url, String data, Function<String, T> resultTransformer, String... headers) throws IOException {
        return post(httpClient, url, data, 200, resultTransformer, headers);
    }

    private <T> T post(CloseableHttpClient httpClient, String url, String data, int expectedHttpCode, Function<String, T> resultTransformer, String... headers) throws IOException {
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
            try (InputStreamReader isr = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8)) {
                try (BufferedReader br = new BufferedReader(isr)) {
                    String responseString = br.lines().collect(Collectors.joining("\n"));
                    if (response.getStatusLine().getStatusCode() == expectedHttpCode) {
                        return resultTransformer.apply(responseString);
                    } else {
                        throw OpenAmFeil.FACTORY.uforventetResponsFraOpenAM(response.getStatusLine().getStatusCode(), responseString).toException();
                    }
                }
            }
        } finally {
            post.reset();
        }
    }

    private String hentAuthorizationCode(CloseableHttpClient httpClient) throws IOException {

        String url = getIssoHostUrl() + "/authorize?response_type=code&scope=openid&client_id=" + getIssoUserName() + "&state=dummy&redirect_uri=" + redirectUriEncoded;
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
            throw OpenAmFeil.FACTORY.kunneIkkeFinneAuthCode(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()).toException();
        } finally {
            get.reset();
        }
    }

    public static String getIssoHostUrl() {
        return getProperty(OPEN_ID_CONNECT_ISSO_HOST);
    }

    public static String getIssoIssuerUrl() {
        return getProperty(OPEN_ID_CONNECT_ISSO_ISSUER);
    }

    public static String getIssoJwksUrl() {
        return getProperty(OPEN_ID_CONNECT_ISSO_JWKS);
    }

    public static String getIssoUserName() {
        return getProperty(OPEN_ID_CONNECT_USERNAME);
    }

    public static String getIssoPassword() {
        return getProperty(OPEN_ID_CONNECT_PASSWORD);
    }
}

