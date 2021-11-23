package no.nav.vedtak.sikkerhet.oidc;

import static no.nav.vedtak.sikkerhet.oidc.WellKnownConfigurationHelper.getIssuerFra;
import static no.nav.vedtak.sikkerhet.oidc.WellKnownConfigurationHelper.getJwksFra;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.OpenAMHelper;

public class OpenIDProviderConfigProvider {
    private static final Environment ENV = Environment.current();

    public static final String OPEN_AM_WELL_KNOWN_URL = ENV.getProperty("oidc.open.am.well.known.url");
    public static final String OPEN_AM_CLIENT_ID = "oidc.open.am.client.id";
    public static final String OPEN_AM_CLIENT_SECRET = "oidc.open.am.client.secret";

    private static final String STS_WELL_KNOWN_URL = ENV.getProperty("oidc.sts.well.known.url");

    private static final String LOGINSERVICE_IDPORTEN_DISCOVERY_URL = ENV.getProperty("loginservice.idporten.discovery.url"); // naiserator
    private static final String LOGINSERVICE_CLIENT_ID = "loginservice.idporten.audience"; // naiserator

    private static final String AZURE_WELL_KNOWS_URL = ENV.getProperty("azure.app.well.known.url"); // naiserator
    private static final String AZURE_CLIENT_ID = "azure.app.client.id"; // naiserator

    private static final String TOKEN_X_WELL_KNOWN_URL = ENV.getProperty("token.x.well.known.url"); // naiserator
    private static final String TOKEN_X_CLIENT_ID = "token.x.client.id"; // naiserator

    public Set<OpenIDProviderConfig> getConfigs() {
        Set<OpenIDProviderConfig> configs = new HashSet<>();

        // OpenAM
        configs.add(createOpenAmConfiguration(OPEN_AM_WELL_KNOWN_URL));

        // OIDC STS
        configs.add(createStsConfiguration(STS_WELL_KNOWN_URL));

        // Azure
        if (AZURE_WELL_KNOWS_URL != null) {
            configs.add(createAzureAppConfiguration(AZURE_WELL_KNOWS_URL));
        }

        // Loginservice
        if (LOGINSERVICE_IDPORTEN_DISCOVERY_URL != null) {
            configs.add(createLoginServiceConfiguration(LOGINSERVICE_IDPORTEN_DISCOVERY_URL));
        }

        // TokenX
        if (TOKEN_X_WELL_KNOWN_URL != null) {
            configs.add(createTokenXConfiguration(TOKEN_X_WELL_KNOWN_URL));
        }

        return configs;
    }

    private OpenIDProviderConfig createOpenAmConfiguration(String wellKnownUrl) {
        return createConfiguration("oidc_openam",
            getIssuerFra(wellKnownUrl).orElse(OpenAMHelper.getIssoIssuerUrl()),
            getJwksFra(wellKnownUrl).orElse(OpenAMHelper.getIssoJwksUrl()),
            false,
            OpenAMHelper.getIssoUserName(),
            true);
    }

    private OpenIDProviderConfig createStsConfiguration(String wellKnownUrl) {
        return createConfiguration("oidc_sts",
            getIssuerFra(wellKnownUrl).orElse(ENV.getRequiredProperty("oidc.sts.issuer.url")),
            getJwksFra(wellKnownUrl).orElse(ENV.getRequiredProperty("oidc.sts.jwks.url")),
            false,
            "Client name is not used for OIDC STS",
            true);
    }

    private OpenIDProviderConfig createAzureAppConfiguration(final String wellKnownUrl) {
        return createConfiguration("oidc_azure",
            getIssuerFra(wellKnownUrl).orElseThrow(),
            getJwksFra(wellKnownUrl).orElseThrow(),
            !ENV.isLocal(),
            ENV.getRequiredProperty(AZURE_CLIENT_ID),
            true);
    }

    private OpenIDProviderConfig createLoginServiceConfiguration(String wellKnownUrl) {
        return createConfiguration("oidc_loginservice",
            getIssuerFra(wellKnownUrl).orElseThrow(),
            getJwksFra(wellKnownUrl).orElseThrow(),
            !ENV.isLocal(),
            ENV.getRequiredProperty(LOGINSERVICE_CLIENT_ID),
            false);
    }

    private OpenIDProviderConfig createTokenXConfiguration(String wellKnownUrl) {
        return createConfiguration("oidc_tokenx",
            getIssuerFra(wellKnownUrl).orElseThrow(),
            getJwksFra(wellKnownUrl).orElseThrow(),
            false,
            ENV.getRequiredProperty(TOKEN_X_CLIENT_ID),
            false);
    }

    private OpenIDProviderConfig createConfiguration(String providerName,
                                                     String issuer,
                                                     String jwks,
                                                     boolean useProxyForJwks,
                                                     String clientName,
                                                     boolean skipAudienceValidation) {
        return Optional.ofNullable(clientName)
                .map(c -> new OpenIDProviderConfig(
                        url(issuer, "issuer", providerName),
                        url(jwks, "jwks", providerName),
                        useProxyForJwks,
                        c,
                    30,
                        skipAudienceValidation))
                .orElseThrow();
    }

    private static URL url(String url, String key, String providerName) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new TekniskException("F-644196",
                    String.format("Syntaksfeil i token validator konfigurasjonen av '%s' for '%s'", key, providerName), e);
        }
    }
}
