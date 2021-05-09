package no.nav.vedtak.sikkerhet.oidc;

import static no.nav.vedtak.isso.OpenAMHelper.getIssoHostUrl;
import static no.nav.vedtak.isso.OpenAMHelper.getIssoIssuerUrl;
import static no.nav.vedtak.isso.OpenAMHelper.getIssoJwksUrl;
import static no.nav.vedtak.isso.OpenAMHelper.getIssoPassword;
import static no.nav.vedtak.isso.OpenAMHelper.getIssuerFra;
import static no.nav.vedtak.isso.OpenAMHelper.getJwksFra;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.eksterneIdentTyper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.sikkerhet.domene.IdentType;

class OpenIDProviderConfigProvider {
    private static final Environment ENV = Environment.current();
    private static final String LOGINSERVICE_IDPORTEN_DISCOVERY_URL = "loginservice.idporten.discovery.url";
    private static final String LOGINSERVICE_IDPORTEN_AUDIENCE = "loginservice.idporten.audience";

    private static final String TOKEN_X_WELL_KNOWN_URL = "token.x.well.known.url";
    private static final String TOKEN_X_CLIENT_ID = "token.x.client.id";

    public static Set<OpenIDProviderConfig> getConfigs() {
        Set<OpenIDProviderConfig> configs = new HashSet<>();
        configs.add(createOpenAmConfiguration(false, 30, true, OidcTokenValidatorProvider.interneIdentTyper));
        configs.add(
                createStsConfiguration(OidcTokenValidatorProvider.PROVIDERNAME_STS, false, 30, true, OidcTokenValidatorProvider.interneIdentTyper));
        configs.add(createOIDCConfiguration(OidcTokenValidatorProvider.PROVIDERNAME_AAD_B2C, !ENV.isLocal(), 30, false, eksterneIdentTyper));
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
    private static OpenIDProviderConfig createOpenAmConfiguration(boolean useProxyForJwks, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation,
            Set<IdentType> identTyper) {
        String providerName = OidcTokenValidatorProvider.PROVIDERNAME_OPEN_AM;
        String clientName = ENV.getProperty(providerName + OidcTokenValidatorProvider.AGENT_NAME_KEY);
        if (clientName != null) {
            return createConfiguration(providerName, useProxyForJwks, allowedClockSkewInSeconds, skipAudienceValidation, identTyper);
        }

        clientName = OpenAMHelper.getIssoUserName();
        String clientPassword = getIssoPassword();
        String issuer = getIssoIssuerUrl();
        String host = getIssoHostUrl();
        String jwks = getIssoJwksUrl();
        return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, clientPassword, host, allowedClockSkewInSeconds,
                skipAudienceValidation, identTyper);
    }

    private static OpenIDProviderConfig createStsConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation, Set<IdentType> identTyper) {
        String issuer = ENV.getProperty(providerName + OidcTokenValidatorProvider.ALT_ISSUER_URL_KEY);
        if (null == issuer) {
            return null;
        }
        String clientName = "Client name is not used for STS";
        String clientPassword = "Client password is not used for STS";
        String host = "https://host.is.not.used.for.STS";
        String jwks = ENV.getProperty(providerName + OidcTokenValidatorProvider.ALT_JWKS_URL_KEY);
        return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, clientPassword, host, allowedClockSkewInSeconds,
                skipAudienceValidation, identTyper);
    }

    private static OpenIDProviderConfig createConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation, Set<IdentType> identTyper) {
        String clientName = ENV.getProperty(providerName + OidcTokenValidatorProvider.AGENT_NAME_KEY);
        String clientPassword = ENV.getProperty(providerName + OidcTokenValidatorProvider.PASSWORD_KEY);
        String issuer = ENV.getProperty(providerName + OidcTokenValidatorProvider.ISSUER_URL_KEY);
        String host = ENV.getProperty(providerName + OidcTokenValidatorProvider.HOST_URL_KEY);
        String jwks = ENV.getProperty(providerName + OidcTokenValidatorProvider.JWKS_URL_KEY);
        return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, clientPassword, host, allowedClockSkewInSeconds,
                skipAudienceValidation, identTyper);
    }

    private static OpenIDProviderConfig createTokenXConfiguration(String wellKnownUrl) {
        return createConfiguration("tokenx",
                getIssuerFra(wellKnownUrl),
                getJwksFra(wellKnownUrl),
                false,
                ENV.getRequiredProperty(TOKEN_X_CLIENT_ID),
                null,
                "http://bare.tull.ikke.brukt",
                30,
                false,
                eksterneIdentTyper);
    }

    private static OpenIDProviderConfig createOIDCConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation, Set<IdentType> identTyper) {
        return createConfiguration(providerName,
                issuer(providerName),
                jwks(providerName),
                useProxyForJwks,
                clientName(providerName),
                ENV.getProperty(providerName + OidcTokenValidatorProvider.PASSWORD_KEY),
                ENV.getProperty(providerName + OidcTokenValidatorProvider.HOST_URL_KEY),
                allowedClockSkewInSeconds,
                skipAudienceValidation,
                identTyper);
    }

    private static String clientName(String providerName) {
        return ENV.getProperty(LOGINSERVICE_IDPORTEN_AUDIENCE, providerName + OidcTokenValidatorProvider.AGENT_NAME_KEY);
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

    private static OpenIDProviderConfig createConfiguration(String providerName, String issuer, String jwks, boolean useProxyForJwks,
            String clientName,
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