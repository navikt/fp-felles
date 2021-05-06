package no.nav.vedtak.sikkerhet.oidc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.sikkerhet.domene.IdentType;

class OpenIDProviderConfigProvider {
    private static final String LOGINSERVICE_IDPORTEN_DISCOVERY_URL = "loginservice.idporten.discovery.url";
    private static final String LOGINSERVICE_IDPORTEN_AUDIENCE = "loginservice.idporten.audience";

    public Set<OpenIDProviderConfig> getConfigs() {
        Set<OpenIDProviderConfig> configs = new HashSet<>();
        configs.add(createOpenAmConfiguration(false, 30, true, OidcTokenValidatorProvider.interneIdentTyper));
        configs.add(
                createStsConfiguration(OidcTokenValidatorProvider.PROVIDERNAME_STS, false, 30, true, OidcTokenValidatorProvider.interneIdentTyper));
        configs.add(createOIDCConfiguration(OidcTokenValidatorProvider.PROVIDERNAME_AAD_B2C, !OidcTokenValidatorProvider.ENV.isLocal(), 30, false,
                OidcTokenValidatorProvider.eksterneIdentTyper));
        configs.remove(null); // Fjerner en eventuell feilet konfigurasjon WTF ?
        return configs;
    }

    /**
     * For bakoverkompabilitet for eksisterende måte å konfigurere opp OIDC Vil
     * benytte ny konfigurasjonsmåte hvis definert
     */
    private OpenIDProviderConfig createOpenAmConfiguration(boolean useProxyForJwks, int allowedClockSkewInSeconds, boolean skipAudienceValidation,
            Set<IdentType> identTyper) {
        String providerName = OidcTokenValidatorProvider.PROVIDERNAME_OPEN_AM;
        String clientName = OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.AGENT_NAME_KEY);
        if (clientName != null) {
            return createConfiguration(providerName, useProxyForJwks, allowedClockSkewInSeconds, skipAudienceValidation, identTyper);
        }

        clientName = OpenAMHelper.getIssoUserName();
        String clientPassword = OpenAMHelper.getIssoPassword();
        String issuer = OpenAMHelper.getIssoIssuerUrl();
        String host = OpenAMHelper.getIssoHostUrl();
        String jwks = OpenAMHelper.getIssoJwksUrl();
        return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, clientPassword, host, allowedClockSkewInSeconds,
                skipAudienceValidation, identTyper);
    }

    private OpenIDProviderConfig createStsConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation, Set<IdentType> identTyper) {
        String issuer = OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.ALT_ISSUER_URL_KEY);
        if (null == issuer) {
            return null;
        }
        String clientName = "Client name is not used for STS";
        String clientPassword = "Client password is not used for STS";
        String host = "https://host.is.not.used.for.STS";
        String jwks = OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.ALT_JWKS_URL_KEY);
        return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, clientPassword, host, allowedClockSkewInSeconds,
                skipAudienceValidation, identTyper);
    }

    private OpenIDProviderConfig createConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation, Set<IdentType> identTyper) {
        String clientName = OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.AGENT_NAME_KEY);
        String clientPassword = OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.PASSWORD_KEY);
        String issuer = OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.ISSUER_URL_KEY);
        String host = OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.HOST_URL_KEY);
        String jwks = OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.JWKS_URL_KEY);
        return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, clientPassword, host, allowedClockSkewInSeconds,
                skipAudienceValidation, identTyper);
    }

    private OpenIDProviderConfig createOIDCConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation, Set<IdentType> identTyper) {
        return createConfiguration(providerName, issuer(providerName), jwks(providerName), useProxyForJwks, clientName(providerName),
                OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.PASSWORD_KEY),
                OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.HOST_URL_KEY), allowedClockSkewInSeconds,
                skipAudienceValidation, identTyper);
    }

    private static String clientName(String providerName) {
        return Optional.ofNullable(OidcTokenValidatorProvider.ENV.getProperty(LOGINSERVICE_IDPORTEN_AUDIENCE))
                .orElse(OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.AGENT_NAME_KEY));
    }

    private static String jwks(String providerName) {
        return Optional.ofNullable(OidcTokenValidatorProvider.ENV.getProperty(LOGINSERVICE_IDPORTEN_DISCOVERY_URL))
                .map(OpenAMHelper::getJwksFra)
                .orElse(OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.JWKS_URL_KEY));
    }

    private static String issuer(String providerName) {
        return Optional.ofNullable(OidcTokenValidatorProvider.ENV.getProperty(LOGINSERVICE_IDPORTEN_DISCOVERY_URL))
                .map(OpenAMHelper::getIssuerFra)
                .orElse(OidcTokenValidatorProvider.ENV.getProperty(providerName + OidcTokenValidatorProvider.ISSUER_URL_KEY));
    }

    private OpenIDProviderConfig createConfiguration(String providerName, String issuer, String jwks, boolean useProxyForJwks, String clientName,
            String clientPassword, String host, int allowedClockSkewInSeconds, boolean skipAudienceValidation, Set<IdentType> identTyper) {
        return Optional.ofNullable(clientName)
                .map(c -> new OpenIDProviderConfig(
                        url(issuer, "issuer", providerName),
                        url(jwks, "jwks", providerName),
                        useProxyForJwks,
                        c,
                        clientPassword,
                        url(host, "host", providerName),
                        allowedClockSkewInSeconds,
                        skipAudienceValidation,
                        identTyper))
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