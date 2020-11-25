package no.nav.vedtak.felles.integrasjon.rest;

import java.net.URI;
import java.util.ArrayList;
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
    private static final String GRANT_TYPE = "grant_type";
    private static final String SCOPE = "scope";
    private static final List<NameValuePair> params = List.of(
        new BasicNameValuePair(GRANT_TYPE, "client_credentials"),
        new BasicNameValuePair(SCOPE, "openid"));
    private String username;
    private String password;
    private String stsUri;
    private String tokenEndpointPath;

    StsAccessTokenConfig() {
        //CDI
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

    URI getStsURI() {
        return UriBuilder.fromUri(stsUri)
            .path(tokenEndpointPath)
            .build();
    }

    List<NameValuePair> getFormParams() {
        return params;
    }

}
