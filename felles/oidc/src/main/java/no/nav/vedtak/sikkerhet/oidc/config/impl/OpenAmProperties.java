package no.nav.vedtak.sikkerhet.oidc.config.impl;

import static no.nav.vedtak.sikkerhet.oidc.config.impl.OidcProviderConfig.OPEN_AM_CLIENT_ID;
import static no.nav.vedtak.sikkerhet.oidc.config.impl.OidcProviderConfig.OPEN_AM_CLIENT_SECRET;
import static no.nav.vedtak.sikkerhet.oidc.config.impl.OidcProviderConfig.OPEN_AM_WELL_KNOWN_URL;

import java.util.Optional;

import no.nav.foreldrepenger.konfig.Environment;

public class OpenAmProperties {


    private static final Environment ENV = Environment.current();

    public static final String OPEN_ID_CONNECT_ISSO_HOST = "OpenIdConnect.issoHost";
    public static final String OPEN_ID_CONNECT_ISSO_ISSUER = "OpenIdConnect.issoIssuer";
    public static final String OPEN_ID_CONNECT_ISSO_JWKS = "OpenIdConnect.issoJwks";
    public static final String OPEN_ID_CONNECT_USERNAME = "OpenIdConnect.username";
    public static final String OPEN_ID_CONNECT_PASSWORD = "OpenIdConnect.password";
    public static final String ISSUER_KEY = "issuer";
    public static final String JWKS_KEY = "jwks_uri";

    public static final String WELL_KNOWN_ENDPOINT = "/.well-known/openid-configuration";


    public OpenAmProperties() {
    }

    public static String getIssoHostUrl() {
        return ENV.getProperty(OPEN_ID_CONNECT_ISSO_HOST);
    }

    public static String getIssoUserName() {
        return Optional.ofNullable(ENV.getProperty(OPEN_AM_CLIENT_ID))
            .orElseGet(() -> ENV.getProperty(OPEN_ID_CONNECT_USERNAME));
    }

    public static String getIssoPassword() {
        return Optional.ofNullable(ENV.getProperty(OPEN_AM_CLIENT_SECRET))
            .orElseGet(() -> ENV.getProperty(OPEN_ID_CONNECT_PASSWORD));
    }

    public static String getIssoIssuerUrl() {
        return Optional.ofNullable(ENV.getProperty(OPEN_ID_CONNECT_ISSO_ISSUER))
            .or(() -> Optional.ofNullable(getOpenAmWellKnownConfig().issuer()))
            .orElseGet(OpenAmProperties::getIssoHostUrl);
    }

    public static String getIssoJwksUrl() {
        return Optional.ofNullable(ENV.getProperty(OPEN_ID_CONNECT_ISSO_JWKS))
            .orElseGet(() -> getOpenAmWellKnownConfig().jwks_uri());
    }

    public static String getIssoTokenUrl() {
        return getIssoIssuerUrl() + "/access-token";
    }

    public static String getIssoAuthUrl() {
        return getIssoIssuerUrl() + "/authorize";
    }

    private static WellKnownOpenIdConfiguration getOpenAmWellKnownConfig() {
        var discoveryURL = Optional.ofNullable(ENV.getProperty(OPEN_AM_WELL_KNOWN_URL))
            .orElseGet(() -> getIssoHostUrl() + WELL_KNOWN_ENDPOINT);
        return WellKnownConfigurationHelper.getWellKnownConfig(discoveryURL, null);
    }

}
