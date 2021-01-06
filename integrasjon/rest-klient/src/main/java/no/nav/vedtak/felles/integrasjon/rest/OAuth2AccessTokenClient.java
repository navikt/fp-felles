package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import no.nav.vedtak.felles.integrasjon.rest.jersey.OAuth2AccessTokenJerseyClient;

/**
 *
 * @deprecated Erstattes av {@link OAuth2AccessTokenJerseyClient}
 *
 */
@Deprecated(since = "3.0.x", forRemoval = true)
class OAuth2AccessTokenClient {
    private static final ObjectMapper mapper = DefaultJsonMapper.getObjectMapper();

    private final CloseableHttpClient closeableHttpClient;
    private final URI tokenEndpoint;
    private final URI tokenEndpointProxy;
    private final String clientId;
    private final String clientSecret;
    private final RequestConfig requestConfig;

    OAuth2AccessTokenClient(
            CloseableHttpClient closeableHttpClient,
            URI tokenEndpoint,
            URI tokenEndpointProxy,
            String clientId,
            String clientSecret) {
        this.closeableHttpClient = closeableHttpClient;
        this.tokenEndpoint = tokenEndpoint;
        this.tokenEndpointProxy = tokenEndpointProxy;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.requestConfig = requestConfig(tokenEndpointProxy);
    }

    String hentAccessToken(Set<String> scopes) {
        return hentTokens(scopes).get("access_token").asText();
    }

    private ObjectNode hentTokens(Set<String> scopes) {
        var entity = entity(scopes);
        var httpPost = httpPost(entity);
        String responseEntity;
        try {
            responseEntity = closeableHttpClient.execute(httpPost, new BasicResponseHandler());
        } catch (IOException e) {
            throw OidcRestClientFeil.FACTORY.ioException(tokenEndpoint, e).toException();
        }
        try {
            return (ObjectNode) mapper.readTree(responseEntity);
        } catch (JsonProcessingException e) {
            throw DefaultJsonMapper.DefaultJsonMapperFeil.FACTORY.kunneIkkeSerialisereJson(e).toException();
        }
    }

    private String entity(Set<String> scopes) {
        return "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret + "&scope=" + String.join(" ", scopes);
    }

    private static RequestConfig requestConfig(URI tokenEndpointProxy) {
        if (tokenEndpointProxy == null)
            return null;
        HttpHost httpHostProxy = HttpHost.create(tokenEndpointProxy.toString());
        return RequestConfig.custom()
                .setProxy(httpHostProxy)
                .build();
    }

    private HttpPost httpPost(String entity) {
        HttpPost post = new HttpPost(tokenEndpoint);
        post.setEntity(new StringEntity(entity, StandardCharsets.UTF_8));
        post.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());
        if (requestConfig != null) {
            post.setConfig(requestConfig);
        }
        return post;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<tokenEndpoint=" + tokenEndpoint + ", clientId=" + clientId + ", clientSecret=***, tokenEndpointProxy="
                + tokenEndpointProxy + ">";
    }
}
