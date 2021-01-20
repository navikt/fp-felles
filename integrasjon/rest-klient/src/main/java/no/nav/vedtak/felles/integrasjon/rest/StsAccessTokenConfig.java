package no.nav.vedtak.felles.integrasjon.rest;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class StsAccessTokenConfig {

    private static final String DEFAULT_PATH = "/rest/v1/sts/token";
    public static final String GRANT_TYPE = "grant_type";
    public static final String SCOPE = "scope";
    public static final List<NameValuePair> params = List.of(
            new BasicNameValuePair(GRANT_TYPE, "client_credentials"),
            new BasicNameValuePair(SCOPE, "openid"));
    private final String username;
    private final String password;
    private final String stsUri;
    private final String tokenEndpointPath;

    public StsAccessTokenConfig(String issuerUrl, String username, String password) {
        this(issuerUrl, DEFAULT_PATH, username, password);
    }

    @Inject
    StsAccessTokenConfig(@KonfigVerdi("oidc.sts.issuer.url") String issuerUrl,
            @KonfigVerdi(value = "oidc.sts.token.path", defaultVerdi = DEFAULT_PATH, required = false) String tokenEndpointPath,
            @KonfigVerdi("systembruker.username") String username,
            @KonfigVerdi("systembruker.password") String password) {
        this.username = username;
        this.password = password;
        this.stsUri = issuerUrl;
        this.tokenEndpointPath = tokenEndpointPath;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public URI getStsURI() {
        return UriBuilder.fromUri(stsUri)
                .path(tokenEndpointPath)
                .build();
    }

    public List<NameValuePair> getFormParams() {
        return params;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [username=" + username + ", stsUri=" + stsUri + ", tokenEndpointPath=" + tokenEndpointPath + "]";
    }

}
