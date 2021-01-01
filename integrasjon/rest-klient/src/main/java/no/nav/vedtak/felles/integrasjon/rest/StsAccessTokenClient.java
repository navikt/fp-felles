package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.log.mdc.MDCOperations;

public class StsAccessTokenClient {
    private static final ObjectMapper mapper = DefaultJsonMapper.getObjectMapper();

    private final HttpClient httpClient;
    private final StsAccessTokenConfig config;

    StsAccessTokenClient(HttpClient httpClient, StsAccessTokenConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public String getUsername() {
        return config.getUsername();
    }

    public String hentAccessToken() {
        String responseEntity;
        try {
            responseEntity = httpClient.execute(httpPost(), new BasicResponseHandler());
        } catch (IOException e) {
            throw OidcRestClientFeil.FACTORY.ioException(config.getStsURI(), e).toException();
        }
        try {
            return mapper.readTree(responseEntity).get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw DefaultJsonMapper.DefaultJsonMapperFeil.FACTORY.kunneIkkeSerialisereJson(e).toException();
        }
    }

    private HttpPost httpPost() throws UnsupportedEncodingException {

        HttpPost post = new HttpPost(config.getStsURI());
        post.setHeader("Authorization", basicCredentials(config.getUsername(), config.getPassword()));
        post.setHeader("Nav-Call-Id", MDCOperations.getCallId());
        post.setHeader("Cache-Control", "no-cache");
        post.setHeader("Nav-Consumer-Id", config.getUsername());
        post.setEntity(new UrlEncodedFormEntity(config.getFormParams()));
        return post;
    }

    static String basicCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8));
    }
}
