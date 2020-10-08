package no.nav.vedtak.felles.integrasjon.rest;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class StsAccessTokenConfig {

    private static final String DEFAULT_PATH = "rest/v1/sts/token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String SCOPE = "scope";
    private String username;
    private String password;
    private String stsUri;

    StsAccessTokenConfig() {
        //CDI
    }

    @Inject
    StsAccessTokenConfig(@KonfigVerdi("oidc.sts.issuer.url") String issuerUrl,
                         @KonfigVerdi("systembruker.username") String username,
                         @KonfigVerdi("systembruker.password") String password) {
        this.username = username;
        this.password = password;
        this.stsUri = issuerUrl;

    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    URI getStsURI() {
        return UriBuilder.fromUri(stsUri)
            .path(DEFAULT_PATH)
            .queryParam(GRANT_TYPE, "client_credentials")
            .queryParam(SCOPE, "openid")
            .build();
    }
}
