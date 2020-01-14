package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.vedtak.konfig.PropertyUtil.getProperty;

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

    private static final Logger LOG = LoggerFactory.getLogger(OidcTokenValidatorProvider.class);
    private static final Set<IdentType> interneIdentTyper = new HashSet<>(Arrays.asList(IdentType.InternBruker, IdentType.Systemressurs));
    private static final Set<IdentType> eksterneIdentTyper = new HashSet<>(Arrays.asList(IdentType.EksternBruker));

    private static OidcTokenValidatorProvider instance;
    private final Map<String, OidcTokenValidator> validators;

    private OidcTokenValidatorProvider() {
        validators = init();
    }

    private OidcTokenValidatorProvider(Map<String, OidcTokenValidator> validators) {
        this.validators = validators;
    }

    public static OidcTokenValidatorProvider instance() {
        if (instance == null) {
            synchronized (OidcTokenValidatorProvider.class) {
                if (instance == null) {
                    instance = new OidcTokenValidatorProvider();
                }
            }
        }
        return instance;
    }

    // For test
    static void clearInstance() {
        synchronized (OidcTokenValidatorProvider.class) {
            instance = null;
        }
    }

    // For test
    static void setValidators(Map<String, OidcTokenValidator> validators) {
        synchronized (OidcTokenValidatorProvider.class) {
            instance = new OidcTokenValidatorProvider(validators);
        }
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

    static class OpenIDProviderConfigProvider{
        public Set<OpenIDProviderConfig> getConfigs() {
            Set<OpenIDProviderConfig> configs = new HashSet<>();
            configs.add(createOpenAmConfiguration(false,30, true, interneIdentTyper));
            configs.add(createStsConfiguration(PROVIDERNAME_STS, false, 30, true, interneIdentTyper));
            configs.add(createConfiguration(PROVIDERNAME_AAD_B2C, true, 30, false, eksterneIdentTyper));
            configs.remove(null); // Fjerner en eventuell feilet konfigurasjon
            return configs;
        }

        /**
         * For bakoverkompabilitet for eksisterende måte å konfigurere opp OIDC
         * Vil benytte ny konfigurasjonsmåte hvis definert
         */
        private OpenIDProviderConfig createOpenAmConfiguration(boolean useProxyForJwks, int allowedClockSkewInSeconds, boolean skipAudienceValidation, Set<IdentType> identTyper) {
            String providerName = PROVIDERNAME_OPEN_AM;
            String clientName = getProperty(providerName + AGENT_NAME_KEY);
            if (clientName != null) {
                return createConfiguration(providerName, useProxyForJwks, allowedClockSkewInSeconds, skipAudienceValidation, identTyper);
            }

            clientName = OpenAMHelper.getIssoUserName();
            String clientPassword = OpenAMHelper.getIssoPassword();
            String issuer = OpenAMHelper.getIssoIssuerUrl();
            String host = OpenAMHelper.getIssoHostUrl();
            String jwks = OpenAMHelper.getIssoJwksUrl();
            return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, clientPassword, host, allowedClockSkewInSeconds, skipAudienceValidation, identTyper);
        }

        private OpenIDProviderConfig createStsConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds, boolean skipAudienceValidation, Set<IdentType> identTyper) {
            String issuer = getProperty(providerName + ALT_ISSUER_URL_KEY);
            if(null == issuer){
                return null;
            }
            String clientName = "Client name is not used for STS";
            String clientPassword = "Client password is not used for STS";
            String host = "https://host.is.not.used.for.STS";
            String jwks = getProperty(providerName + ALT_JWKS_URL_KEY);
            return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, clientPassword, host, allowedClockSkewInSeconds, skipAudienceValidation, identTyper);
        }

        private OpenIDProviderConfig createConfiguration(String providerName, boolean useProxyForJwks, int allowedClockSkewInSeconds, boolean skipAudienceValidation, Set<IdentType> identTyper) {
            String clientName = getProperty(providerName + AGENT_NAME_KEY);
            String clientPassword = getProperty(providerName + PASSWORD_KEY);
            String issuer = getProperty(providerName + ISSUER_URL_KEY);
            String host = getProperty(providerName + HOST_URL_KEY);
            String jwks = getProperty(providerName + JWKS_URL_KEY);
            return createConfiguration(providerName, issuer, jwks, useProxyForJwks, clientName, clientPassword, host, allowedClockSkewInSeconds, skipAudienceValidation, identTyper);
        }

        private OpenIDProviderConfig createConfiguration(String providerName, String issuer, String jwks, boolean useProxyForJwks, String clientName, String clientPassword, String host, int allowedClockSkewInSeconds, boolean skipAudienceValidation, Set<IdentType> identTyper) {
            if (null == clientName) {
                return null;
            }
            URL issuerUrl;
            URL jwksUrl;
            URL hostUrl;
            String key = "";
            try {
                key = "issuer";
                issuerUrl = new URL(issuer);
                key = "jwks";
                jwksUrl = new URL(jwks);
                key = "host";
                hostUrl = new URL(host);
            } catch (MalformedURLException e) {
                throw TokenProviderFeil.FACTORY.feilIKonfigurasjonAvOidcProvider(key,providerName, e).toException();
            }
            OpenIDProviderConfig config = new OpenIDProviderConfig(
                    issuerUrl,
                    jwksUrl,
                    useProxyForJwks,
                    clientName,
                    clientPassword,
                    hostUrl,
                    allowedClockSkewInSeconds,
                    skipAudienceValidation,
                    identTyper);

            return config;
        }

    }
}
