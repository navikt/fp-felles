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
    private static final String LOGINSERVICE_IDPORTEN_DISCOVERY_URL = "loginservice.idporten.discovery.url";
    private static final String LOGINSERVICE_IDPORTEN_AUDIENCE = "loginservice.idporten.audience";

    private static final String TOKEN_X_WELL_KNOWN_URL = "token.x.well.known.url";
    private static final String TOKEN_X_CLIENT_ID = "token.x.client.id";

    public Set<OpenIDProviderConfig> getConfigs() {
        Set<OpenIDProviderConfig> configs = new HashSet<>();
        configs.add(createOpenAmConfiguration(false, 30, true));
        configs.add(
                stsConfiguration(OidcTokenValidatorProvider.PROVIDERNAME_STS, false, 30, true));
        configs.add(createOIDCConfiguration(OidcTokenValidatorProvider.PROVIDERNAME_AAD_B2C, !ENV.isLocal(), 30, false));
        if (ENV.getProperty(TOKEN_X_WELL_KNOWN_URL) != null) {
            configs.add(createTokenXConfiguration(ENV.getProperty(TOKEN_X_WELL_KNOWN_URL)));
        }
        configs.remove(null); // Fjerner en eventuell feilet konfigurasjon WTF ?
        return configs;
    }

    /**
     * For bakoverkompabilitet for eksisterende måte å konfigurere opp OIDC Vil
     * benytte ny konfigurasjonsmåte hvis definert
     */
    private OpenIDProviderConfig createOpenAmConfiguration(boolean useProxyForJwks, int allowedClockSkewInSeconds, boolean skipAudienceValidation) {
        String providerName = OidcTokenValidatorProvider.PROVIDERNAME_OPEN_AM;
        String clientName = ENV.getProperty(providerName + OidcTokenValidatorProvider.AGENT_NAME_KEY);
        if (clientName != null) {
            return createConfiguration(providerName, useProxyForJwks, allowedClockSkewInSeconds, skipAudienceValidation);
        }

        clientName = OpenAMHelper.getIssoUserName();
        String issuer = OpenAMHelper.getIssoIssuerUrl();
        String jwks = OpenAMHelper.getIssoJwksUrl();
        return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, allowedClockSkewInSeconds,
                skipAudienceValidation);
    }

    private OpenIDProviderConfig stsConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation) {
        String issuer = ENV.getProperty(providerName + OidcTokenValidatorProvider.ALT_ISSUER_URL_KEY);
        if (null == issuer) {
            return null;
        }
        String clientName = "Client name is not used for STS";
        String jwks = ENV.getProperty(providerName + OidcTokenValidatorProvider.ALT_JWKS_URL_KEY);
        return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, allowedClockSkewInSeconds,
                skipAudienceValidation);
    }

    private OpenIDProviderConfig createTokenXConfiguration(String wellKnownUrl) {
        return createConfiguration("tokenx",
                getIssuerFra(wellKnownUrl),
                getJwksFra(wellKnownUrl),
                false,
                ENV.getRequiredProperty(TOKEN_X_CLIENT_ID),
                30,
                false);
    }

    private OpenIDProviderConfig createConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation) {
        String clientName = ENV.getProperty(providerName + OidcTokenValidatorProvider.AGENT_NAME_KEY);
        String issuer = ENV.getProperty(providerName + OidcTokenValidatorProvider.ISSUER_URL_KEY);
        String jwks = ENV.getProperty(providerName + OidcTokenValidatorProvider.JWKS_URL_KEY);
        return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, allowedClockSkewInSeconds,
                skipAudienceValidation);
    }

    private OpenIDProviderConfig createOIDCConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation) {
        return createConfiguration(providerName, issuer(providerName), jwks(providerName), useProxyForJwks, clientName(providerName),
                allowedClockSkewInSeconds,
                skipAudienceValidation);
    }

    private static String clientName(String providerName) {
        return Optional.ofNullable(ENV.getProperty(LOGINSERVICE_IDPORTEN_AUDIENCE))
                .orElse(ENV.getProperty(providerName + OidcTokenValidatorProvider.AGENT_NAME_KEY));
    }

    private static String jwks(String providerName) {
        return Optional.ofNullable(ENV.getProperty(LOGINSERVICE_IDPORTEN_DISCOVERY_URL))
                .map(OpenAMHelper::getJwksFra)
                .orElse(ENV.getProperty(providerName + OidcTokenValidatorProvider.JWKS_URL_KEY));
    }

    private static String issuer(String providerName) {
        return Optional.ofNullable(ENV.getProperty(LOGINSERVICE_IDPORTEN_DISCOVERY_URL))
                .map(OpenAMHelper::getIssuerFra)
                .orElse(ENV.getProperty(providerName + OidcTokenValidatorProvider.ISSUER_URL_KEY));
    }

    private OpenIDProviderConfig createConfiguration(String providerName, String issuer, String jwks, boolean useProxyForJwks, String clientName,
            int allowedClockSkewInSeconds, boolean skipAudienceValidation) {
        return Optional.ofNullable(clientName)
                .map(c -> new OpenIDProviderConfig(
                        url(issuer, "issuer", providerName),
                        url(jwks, "jwks", providerName),
                        useProxyForJwks,
                        c,
                        allowedClockSkewInSeconds,
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