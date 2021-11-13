package no.nav.vedtak.sikkerhet.oidc;

import static no.nav.vedtak.isso.OpenAMHelper.getIssuerFra;
import static no.nav.vedtak.isso.OpenAMHelper.getJwksFra;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.OpenAMHelper;

class OpenIDProviderConfigProvider {
    private static final Environment ENV = Environment.current();

    private static final String OPEN_AM_WELL_KNOWN_URL = ENV.getProperty("open.am.well.known.url");
    private static final String OPEN_AM_CLIENT_ID = "open.am.client.id";

    private static final String GANDALF_STS_WELL_KNOWN_URL = ENV.getProperty("gandalf.sts.well.known.url");

    private static final String LOGINSERVICE_IDPORTEN_DISCOVERY_URL = ENV.getProperty("loginservice.idporten.discovery.url");
    private static final String LOGINSERVICE_CLIENT_ID = "loginservice.idporten.audience";

    private static final String AZURE_WELL_KNOWS_URL = ENV.getProperty("azure.app.well.known.url");
    private static final String AZURE_CLIENT_ID = "azure.app.client.id";

    private static final String TOKEN_X_WELL_KNOWN_URL = ENV.getProperty("token.x.well.known.url");
    private static final String TOKEN_X_CLIENT_ID = "token.x.client.id";

    public Set<OpenIDProviderConfig> getConfigs() {
        Set<OpenIDProviderConfig> configs = new HashSet<>();

        // OpenAM
        if (OPEN_AM_WELL_KNOWN_URL != null) {
            configs.add(createOpenAmConfiguration(OPEN_AM_WELL_KNOWN_URL));
        } else { // fallback til gammel måte
            configs.add(createOpenAmConfiguration());
        }

        // Gandalf STS
        if (GANDALF_STS_WELL_KNOWN_URL != null) {
            configs.add(createGandalfStsConfiguration(GANDALF_STS_WELL_KNOWN_URL));
        } else {
            configs.add(createStsConfiguration());
        }

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

    /**
     * For bakoverkompabilitet for eksisterende måte å konfigurere opp OIDC mot openAm
     */
    private OpenIDProviderConfig createOpenAmConfiguration() {
        var clientName = OpenAMHelper.getIssoUserName();
        var  issuer = OpenAMHelper.getIssoIssuerUrl();
        var  jwks = OpenAMHelper.getIssoJwksUrl();
        return createConfiguration("openam", issuer, jwks, false, clientName,
            true);
    }

    /**
     * For bakoverkompabilitet for eksisterende måte å konfigurere opp OIDC mot gandalf sts
     */
    private OpenIDProviderConfig createStsConfiguration() {
        String clientName = "Client name is not used for Gandalf STS";
        String issuer = ENV.getProperty("oidc.sts.issuer.url");
        String jwks = ENV.getProperty("oidc.sts.jwks.url");
        return createConfiguration("gandalfsts", issuer, jwks, false, clientName,
            true);
    }

    private OpenIDProviderConfig createOpenAmConfiguration(String wellKnownUrl) {
        return createConfiguration("openam",
            getIssuerFra(wellKnownUrl),
            getJwksFra(wellKnownUrl),
            false,
            ENV.getRequiredProperty(OPEN_AM_CLIENT_ID),
            true);
    }

    private OpenIDProviderConfig createGandalfStsConfiguration(String wellKnownUrl) {
        return createConfiguration("gandalfsts",
            getIssuerFra(wellKnownUrl),
            getJwksFra(wellKnownUrl),
            false,
            "Client name is not used for Gandalf STS",
            true);
    }

    private OpenIDProviderConfig createAzureAppConfiguration(final String wellKnownUrl) {
        return createConfiguration("azure",
            getIssuerFra(wellKnownUrl),
            getJwksFra(wellKnownUrl),
            !ENV.isLocal(),
            ENV.getRequiredProperty(AZURE_CLIENT_ID),
            true);
    }

    private OpenIDProviderConfig createLoginServiceConfiguration(String wellKnownUrl) {
        return createConfiguration("loginservice",
            getIssuerFra(wellKnownUrl),
            getJwksFra(wellKnownUrl),
            !ENV.isLocal(),
            ENV.getRequiredProperty(LOGINSERVICE_CLIENT_ID),
            false);
    }

    private OpenIDProviderConfig createTokenXConfiguration(String wellKnownUrl) {
        return createConfiguration("tokenx",
            getIssuerFra(wellKnownUrl),
            getJwksFra(wellKnownUrl),
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
