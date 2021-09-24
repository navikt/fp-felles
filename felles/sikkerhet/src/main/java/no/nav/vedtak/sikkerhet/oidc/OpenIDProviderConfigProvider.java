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

    private static final String OPEN_AM_WELL_KNOWN_URL = "open.am.well.known.url";
    private static final String OPEN_AM_CLIENT_ID = "open.am.client.id";
    private static final String GANDALF_STS_WELL_KNOWN_URL = "gandalf.sts.well.known.url";
    private static final String LOGINSERVICE_IDPORTEN_DISCOVERY_URL = "loginservice.idporten.discovery.url";
    private static final String LOGINSERVICE_CLIENT_ID = "loginservice.idporten.audience";
    private static final String TOKEN_X_WELL_KNOWN_URL = "token.x.well.known.url";
    private static final String TOKEN_X_CLIENT_ID = "token.x.client.id";

    public Set<OpenIDProviderConfig> getConfigs() {
        Set<OpenIDProviderConfig> configs = new HashSet<>();

        // OpenAM
        if (ENV.getProperty(OPEN_AM_WELL_KNOWN_URL) != null) {
            configs.add(createOpenAmConfiguration(ENV.getProperty(OPEN_AM_WELL_KNOWN_URL)));
        } else { // fallback til gammel måte
            configs.add(createOpenAmConfiguration());
        }

        // Gandalf STS
        if (ENV.getProperty(GANDALF_STS_WELL_KNOWN_URL) != null) {
            configs.add(createGandalfStsConfiguration(ENV.getProperty(GANDALF_STS_WELL_KNOWN_URL)));
        } else {
            configs.add(createStsConfiguration());
        }

        // Loginservice
        if (ENV.getProperty(LOGINSERVICE_IDPORTEN_DISCOVERY_URL) != null) {
            configs.add(createLoginServiceConfiguration(ENV.getProperty(LOGINSERVICE_IDPORTEN_DISCOVERY_URL)));
        }

        // TokenX
        if (ENV.getProperty(TOKEN_X_WELL_KNOWN_URL) != null) {
            configs.add(createTokenXConfiguration(ENV.getProperty(TOKEN_X_WELL_KNOWN_URL)));
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
        String issuer = ENV.getProperty("oidc_sts.issuer.url");
        String jwks = ENV.getProperty("oidc_sts.jwks.url");
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

    private OpenIDProviderConfig createConfiguration(String providerName, String issuer, String jwks, boolean useProxyForJwks, String clientName,
                                                     boolean skipAudienceValidation) {
        return Optional.ofNullable(clientName)
                .map(c -> new OpenIDProviderConfig(
                        url(issuer, "issuer", providerName),
                        url(jwks, "jwks", providerName),
                        useProxyForJwks,
                        c,
                    30,
                        skipAudienceValidation))
                .orElse(null);

    }

    private static URL url(String url, String key, String providerName) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new TekniskException("F-644196",
                    String.format("Syntaksfeil i OIDC konfigurasjonen av '%s' for '%s'", key, providerName), e);
        }
    }

}
