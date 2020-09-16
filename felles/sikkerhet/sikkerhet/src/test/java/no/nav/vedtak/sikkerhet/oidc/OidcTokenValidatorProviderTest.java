package no.nav.vedtak.sikkerhet.oidc;

import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_ISSO_HOST;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_PASSWORD;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_USERNAME;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.AGENT_NAME_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.ALT_ISSUER_URL_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.ALT_JWKS_URL_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.HOST_URL_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.ISSUER_URL_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.JWKS_URL_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.PROVIDERNAME_AAD_B2C;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.PROVIDERNAME_OPEN_AM;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.PROVIDERNAME_STS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jose4j.json.JsonUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.log.util.MemoryAppender;
import no.nav.vedtak.sikkerhet.domene.IdentType;

public class OidcTokenValidatorProviderTest {

    private static final String openam_iss = "https://openam.no";
    private static final String openam_agent = "openam";
    private static final String sts_iss = "https://sts.no";
    private static final String sts_agent = "sts";
    private static final String aad_b2c_iss = "https://aad.b2c.no";
    private static final String aad_b2c_agent = "aad.b2c";

    private static final String validUrl = "https://this.is.a.valid.url";
    private static final String invalidUrl = "this.is.a.invalid.url";
    private static String OPPRINNELIG_OPEN_ID_CONNECT_USERNAME;
    private static String OPPRINNELIG_OPEN_ID_CONNECT_PASSWORD;
    private static String OPPRINNELIG_OPEN_ID_CONNECT_ISSO_HOST;

    private static MemoryAppender logSniffer;
    private static Logger LOG;

    @BeforeAll
    public static void beforeAll() {
        OPPRINNELIG_OPEN_ID_CONNECT_USERNAME = OpenAMHelper.getIssoUserName();
        OPPRINNELIG_OPEN_ID_CONNECT_PASSWORD = OpenAMHelper.getIssoPassword();
        OPPRINNELIG_OPEN_ID_CONNECT_ISSO_HOST = OpenAMHelper.getIssoHostUrl();
        LOG = Logger.class.cast(LoggerFactory.getLogger(OidcTokenValidatorProvider.class));
        LOG.setLevel(Level.INFO);
        logSniffer = new MemoryAppender(LOG.getName());
    }

    @BeforeEach
    public void beforeEach() {
        logSniffer.reset();
        LOG.addAppender(logSniffer);
        logSniffer.start();
        OpenAMHelper.unsetWellKnownConfig();

        System.setProperty(PROVIDERNAME_OPEN_AM + AGENT_NAME_KEY, openam_agent);
        System.setProperty(PROVIDERNAME_OPEN_AM + ISSUER_URL_KEY, openam_iss);
        System.setProperty(PROVIDERNAME_OPEN_AM + JWKS_URL_KEY, validUrl);
        System.setProperty(PROVIDERNAME_OPEN_AM + HOST_URL_KEY, validUrl);

        System.setProperty(PROVIDERNAME_STS + AGENT_NAME_KEY, sts_agent);
        System.setProperty(PROVIDERNAME_STS + ALT_ISSUER_URL_KEY, sts_iss);
        System.setProperty(PROVIDERNAME_STS + ALT_JWKS_URL_KEY, validUrl);
        System.setProperty(PROVIDERNAME_STS + HOST_URL_KEY, validUrl);

        System.setProperty(PROVIDERNAME_AAD_B2C + AGENT_NAME_KEY, aad_b2c_agent);
        System.setProperty(PROVIDERNAME_AAD_B2C + ISSUER_URL_KEY, aad_b2c_iss);
        System.setProperty(PROVIDERNAME_AAD_B2C + JWKS_URL_KEY, validUrl);
        System.setProperty(PROVIDERNAME_AAD_B2C + HOST_URL_KEY, validUrl);

        System.clearProperty("develop-local");
        System.clearProperty("environment.class");
        System.clearProperty("fasit.environment.name");

        OidcTokenValidatorProvider.clearInstance();
    }

    @AfterEach
    public void afterEach() {
        logSniffer.stop();
        LOG.detachAppender(logSniffer);
    }

    @AfterAll
    public static void cleanupState() {
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith(PROVIDERNAME_OPEN_AM) ||
                    key.startsWith(PROVIDERNAME_STS) ||
                    key.startsWith(PROVIDERNAME_AAD_B2C)) {
                System.clearProperty(key);
            }
        }
        if (OPPRINNELIG_OPEN_ID_CONNECT_USERNAME == null) {
            System.clearProperty(OPEN_ID_CONNECT_USERNAME);
        } else {
            System.setProperty(OPEN_ID_CONNECT_USERNAME, OPPRINNELIG_OPEN_ID_CONNECT_USERNAME);
        }
        if (OPPRINNELIG_OPEN_ID_CONNECT_PASSWORD == null) {
            System.clearProperty(OPEN_ID_CONNECT_PASSWORD);
        } else {
            System.setProperty(OPEN_ID_CONNECT_PASSWORD, OPPRINNELIG_OPEN_ID_CONNECT_PASSWORD);
        }
        if (OPPRINNELIG_OPEN_ID_CONNECT_ISSO_HOST == null) {
            System.clearProperty(OPEN_ID_CONNECT_ISSO_HOST);
        } else {
            System.setProperty(OPEN_ID_CONNECT_ISSO_HOST, OPPRINNELIG_OPEN_ID_CONNECT_ISSO_HOST);
        }

    }

    @Test
    public void finner_alle_kjente_providere_og_logger_dem() {
        assertThat(OidcTokenValidatorProvider.instance().getValidator(openam_iss)).isNotNull();
        assertThat(OidcTokenValidatorProvider.instance().getValidator(sts_iss)).isNotNull();
        assertThat(OidcTokenValidatorProvider.instance().getValidator(aad_b2c_iss)).isNotNull();
        assertLogged("Opprettet OidcTokenValidator for ");
        assertLogged("Opprettet OidcTokenValidator for ");
        assertLogged("OpenIDProviderConfig<issuer=https://sts.no>");
        assertLogged("OpenIDProviderConfig<issuer=https://aad.b2c.no>");
        assertLogged("OpenIDProviderConfig<issuer=https://openam.no>");
    }

    @Test
    public void finner_OpenAm_provider_konfigurert_for_OpenAMHelper() {
        cleanupState();
        System.setProperty(OPEN_ID_CONNECT_ISSO_HOST, validUrl);
        System.setProperty(OPEN_ID_CONNECT_USERNAME, openam_agent);
        System.setProperty(OPEN_ID_CONNECT_PASSWORD, "aPassword");
        Map<String, String> testData = new HashMap<>() {
            {
                put(OpenAMHelper.ISSUER_KEY, openam_iss);
                put(OpenAMHelper.JWKS_URI_KEY, validUrl);

            }
        };
        OpenAMHelper.setWellKnownConfig(JsonUtil.toJson(testData));

        assertThat(OidcTokenValidatorProvider.instance().getValidator(openam_iss)).isNotNull();
    }

    @Test
    public void feilkonfigurert_issuer_urler_gir_feil() {
        System.setProperty(PROVIDERNAME_OPEN_AM + ISSUER_URL_KEY, invalidUrl);
        var e = assertThrows(TekniskException.class, () -> OidcTokenValidatorProvider.instance());
        assertTrue(e.getMessage().contains("Syntaksfeil i OIDC konfigurasjonen av 'issuer' for '" + PROVIDERNAME_OPEN_AM));
    }

    @Test
    public void feilkonfigurert_jwks_urler_gir_feil() {
        System.setProperty(PROVIDERNAME_OPEN_AM + JWKS_URL_KEY, invalidUrl);
        var e = assertThrows(TekniskException.class, () -> OidcTokenValidatorProvider.instance());
        assertTrue(e.getMessage().contains("Syntaksfeil i OIDC konfigurasjonen av 'jwks' for '" + PROVIDERNAME_OPEN_AM));

    }

    @Test
    public void feilkonfigurert_host_urler_gir_feil() {
        System.setProperty(PROVIDERNAME_OPEN_AM + HOST_URL_KEY, invalidUrl);
        var e = assertThrows(TekniskException.class, () -> OidcTokenValidatorProvider.instance());
        assertTrue(e.getMessage().contains("Syntaksfeil i OIDC konfigurasjonen av 'host' for '" + PROVIDERNAME_OPEN_AM));
    }

    @Test
    public void interne_providere_skal_IKKE_validere_aud() {
        Set<OpenIDProviderConfig> configs = new OidcTokenValidatorProvider.OpenIDProviderConfigProvider().getConfigs();
        Set<OpenIDProviderConfig> configForInternBruker = configs.stream().filter(config -> config.getIdentTyper().contains(IdentType.InternBruker))
                .collect(Collectors.toSet());
        assertThat(configForInternBruker).allSatisfy(config -> config.isSkipAudienceValidation());
    }

    @Test
    public void eksterne_providere_skal_validere_aud() {
        Set<OpenIDProviderConfig> configs = new OidcTokenValidatorProvider.OpenIDProviderConfigProvider().getConfigs();
        Set<OpenIDProviderConfig> configForEksternBruker = configs.stream().filter(config -> config.getIdentTyper().contains(IdentType.EksternBruker))
                .collect(Collectors.toSet());

        assertThat(configForEksternBruker).allSatisfy(config -> assertThat(config.isSkipAudienceValidation()).isFalse());
    }

    @Test
    public void providere_skal_støtte_enten_InternBruker_eller_EksternBruker() {
        Set<OpenIDProviderConfig> configs = new OidcTokenValidatorProvider.OpenIDProviderConfigProvider().getConfigs();
        assertThat(configs.stream()
                .filter(config -> !config.getIdentTyper().contains(IdentType.InternBruker)
                        && !config.getIdentTyper().contains(IdentType.EksternBruker))
                .collect(Collectors.toSet()))
                        .isEmpty();
    }

    private static void assertLogged(String string) {
        assertThat(logSniffer.searchInfo(string)).isNotNull();
    }
}
