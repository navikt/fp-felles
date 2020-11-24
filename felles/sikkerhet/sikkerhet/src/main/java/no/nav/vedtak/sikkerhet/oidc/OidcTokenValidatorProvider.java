package no.nav.vedtak.sikkerhet.oidc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import no.nav.vedtak.util.env.Environment;

public class OidcTokenValidatorProvider {
    static final String AGENT_NAME_KEY = "agentName";
    static final String PASSWORD_KEY = "password";
    static final String HOST_URL_KEY = "hostUrl";
    static final String ISSUER_URL_KEY = "issuerUrl";
    static final String JWKS_URL_KEY = "jwksUrl";
    static final String ALT_ISSUER_URL_KEY = "issuer.url";
    static final String ALT_JWKS_URL_KEY = "jwks.url";
    static final String PROVIDERNAME_OPEN_AM = "oidc_OpenAM.";
    static final String PROVIDERNAME_STS = "oidc_sts.";
    static final String PROVIDERNAME_AAD_B2C = "oidc_aad_b2c.";

    private static final Environment ENV = Environment.current();

    private static final Logger LOG = LoggerFactory.getLogger(OidcTokenValidatorProvider.class);
    private static final Set<IdentType> interneIdentTyper = new HashSet<>(Arrays.asList(IdentType.InternBruker, IdentType.Systemressurs));
    private static final Set<IdentType> eksterneIdentTyper = new HashSet<>(Arrays.asList(IdentType.EksternBruker));

    private static volatile OidcTokenValidatorProvider instance; // NOSONAR
    private final Map<String, OidcTokenValidator> validators;

    private OidcTokenValidatorProvider() {
        validators = init();
    }

    private OidcTokenValidatorProvider(Map<String, OidcTokenValidator> validators) {
        this.validators = validators;
    }

    public static OidcTokenValidatorProvider instance() {
        var inst = instance;
        if (inst == null) {
            inst = new OidcTokenValidatorProvider();
            instance = inst;
        }
        return inst;
    }

    // For test
    static void clearInstance() {
        instance = null;
    }

    // For test
    static void setValidators(Map<String, OidcTokenValidator> validators) {
        instance = new OidcTokenValidatorProvider(validators);
    }

    public OidcTokenValidator getValidator(String issuer) {
        return validators.get(issuer);
    }

    private Map<String, OidcTokenValidator> init() {
        Set<OpenIDProviderConfig> configs = new OpenIDProviderConfigProvider().getConfigs();
        Map<String, OidcTokenValidator> map = configs.stream().collect(Collectors.toMap(
                config -> config.getIssuer().toExternalForm(),
                config -> new OidcTokenValidator(config)));

        LOG.info("Opprettet OidcTokenValidator for {}", configs);
        return Collections.unmodifiableMap(map);
    }

    static class OpenIDProviderConfigProvider {
        private static final String LOGINSERVICE_IDPORTEN_DISCOVERY_URL = "loginservice.idporten.discovery.url";
        private static final String LOGINSERVICE_IDPORTEN_AUDIENCE = "loginservice.idporten.audience";

        public Set<OpenIDProviderConfig> getConfigs() {
            Set<OpenIDProviderConfig> configs = new HashSet<>();
            configs.add(createOpenAmConfiguration(false, 30, true, interneIdentTyper));
            configs.add(createStsConfiguration(PROVIDERNAME_STS, false, 30, true, interneIdentTyper));
            configs.add(createOIDCConfiguration(PROVIDERNAME_AAD_B2C, !ENV.isLocal(), 30, false, eksterneIdentTyper));
            configs.remove(null); // Fjerner en eventuell feilet konfigurasjon
            return configs;
        }

        /**
         * For bakoverkompabilitet for eksisterende måte å konfigurere opp OIDC Vil
         * benytte ny konfigurasjonsmåte hvis definert
         */
        private OpenIDProviderConfig createOpenAmConfiguration(boolean useProxyForJwks, int allowedClockSkewInSeconds, boolean skipAudienceValidation,
                Set<IdentType> identTyper) {
            String providerName = PROVIDERNAME_OPEN_AM;
            String clientName = ENV.getProperty(providerName + AGENT_NAME_KEY);
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
            String issuer = ENV.getProperty(providerName + ALT_ISSUER_URL_KEY);
            if (null == issuer) {
                return null;
            }
            String clientName = "Client name is not used for STS";
            String clientPassword = "Client password is not used for STS";
            String host = "https://host.is.not.used.for.STS";
            String jwks = ENV.getProperty(providerName + ALT_JWKS_URL_KEY);
            return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, clientPassword, host, allowedClockSkewInSeconds,
                    skipAudienceValidation, identTyper);
        }

        private OpenIDProviderConfig createConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds,
                boolean skipAudienceValidation, Set<IdentType> identTyper) {
            String clientName = ENV.getProperty(providerName + AGENT_NAME_KEY);
            String clientPassword = ENV.getProperty(providerName + PASSWORD_KEY);
            String issuer = ENV.getProperty(providerName + ISSUER_URL_KEY);
            String host = ENV.getProperty(providerName + HOST_URL_KEY);
            String jwks = ENV.getProperty(providerName + JWKS_URL_KEY);
            return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, clientPassword, host, allowedClockSkewInSeconds,
                    skipAudienceValidation, identTyper);
        }

        private OpenIDProviderConfig createOIDCConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds,
                boolean skipAudienceValidation, Set<IdentType> identTyper) {
            return createConfiguration(providerName, issuer(providerName), jwks(providerName), useProxyForJwks, clientName(providerName),
                    ENV.getProperty(providerName + PASSWORD_KEY), ENV.getProperty(providerName + HOST_URL_KEY), allowedClockSkewInSeconds,
                    skipAudienceValidation, identTyper);
        }

        private static String clientName(String providerName) {
            return Optional.ofNullable(ENV.getProperty(LOGINSERVICE_IDPORTEN_AUDIENCE))
                    .orElse(ENV.getProperty(providerName + AGENT_NAME_KEY));
        }

        private static String jwks(String providerName) {
            return Optional.ofNullable(ENV.getProperty(LOGINSERVICE_IDPORTEN_DISCOVERY_URL))
                    .map(OpenAMHelper::getJwksFra)
                    .orElse(ENV.getProperty(providerName + JWKS_URL_KEY));
        }

        private static String issuer(String providerName) {
            return Optional.ofNullable(ENV.getProperty(LOGINSERVICE_IDPORTEN_DISCOVERY_URL))
                    .map(OpenAMHelper::getIssuerFra)
                    .orElse(ENV.getProperty(providerName + ISSUER_URL_KEY));
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
                throw TokenProviderFeil.FACTORY.feilIKonfigurasjonAvOidcProvider(key, providerName, e).toException();
            }
        }

    }
}
