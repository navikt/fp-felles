package no.nav.vedtak.sikkerhet.oidc.config;

import java.util.Optional;

import no.nav.vedtak.sikkerhet.oidc.config.impl.OidcProviderConfig;

public final class ConfigProvider {

    public static Optional<OpenIDConfiguration> getOpenIDConfiguration(OpenIDProvider type) {
        return OidcProviderConfig.instance().getOidcConfig(type);
    }

    public static Optional<OpenIDConfiguration> getOpenIDConfiguration(String issuer) {
        return OidcProviderConfig.instance().getOidcConfig(issuer);
    }

}
