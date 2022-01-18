package no.nav.vedtak.felles.integrasjon.rest.tokenhenter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.foreldrepenger.felles.integrasjon.rest.DefaultJsonMapper;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.oidc.OidcProvider;
import no.nav.vedtak.sikkerhet.oidc.OidcProviderConfig;
import no.nav.vedtak.sikkerhet.oidc.OidcProviderType;


public class StsAccessTokenKlient {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(StsAccessTokenKlient.class);
    private static final ObjectReader READER = DefaultJsonMapper.getObjectMapper().readerFor(OidcTokenResponse.class);

    private static final List<NameValuePair> PARAMS = List.of(
        new BasicNameValuePair("grant_type", "client_credentials"),
        new BasicNameValuePair("scope", "openid"));

    private static final String CLIENT_ID = ENV.getProperty("systembruker.username");
    private static final String CLIENT_SECRET = ENV.getProperty("systembruker.password");
    private static final URI TOKEN_ENDPOINT = OidcProviderConfig.instance()
        .getOidcProvider(OidcProviderType.STS).map(OidcProvider::getTokenEndpoint).orElseThrow();

    private static String accessToken;
    private static long accessTokenExpiry;

    public static synchronized String hentAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < accessTokenExpiry) {
            return accessToken;
        }
        var token = hentToken();
        LOG.info("STS hentet og fikk token av type {} utløper {}", token.token_type(), token.expires_in());
        accessToken = token.access_token();
        var exipry = token.expires_in() < 300 ? 10L : token.expires_in() - 240; // Sekunder
        accessTokenExpiry = System.currentTimeMillis() + (exipry * 1000); // Millisekunder
        return accessToken;
    }

    private static OidcTokenResponse hentToken() {
        String responseEntity;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            responseEntity = httpClient.execute(httpPost(), new BasicResponseHandler());
        } catch (IOException e) {
            throw new TekniskException("F-432937", String.format("IOException ved kommunikasjon med server [%s]", TOKEN_ENDPOINT), e);
        }
        if (responseEntity == null) {
            throw new TekniskException("F-157385", "Kunne ikke hente STS token");
        }
        try {
            return READER.readValue(responseEntity);
        } catch (JsonProcessingException e) {
            throw new TekniskException("F-208314", "Kunne ikke serialisere objekt til JSON", e);
        }
    }

    private static HttpPost httpPost() throws UnsupportedEncodingException {

        HttpPost post = new HttpPost(TOKEN_ENDPOINT);
        post.setHeader("Authorization", basicCredentials(CLIENT_ID, CLIENT_SECRET));
        post.setHeader("Nav-Call-Id", MDCOperations.getCallId());
        post.setHeader("Cache-Control", "no-cache");
        post.setHeader("Nav-Consumer-Id", CLIENT_ID);
        post.setEntity(new UrlEncodedFormEntity(PARAMS));
        return post;
    }

    private static String basicCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8));
    }
}
