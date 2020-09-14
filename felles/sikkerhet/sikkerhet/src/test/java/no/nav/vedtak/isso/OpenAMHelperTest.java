package no.nav.vedtak.isso;

import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_ISSO_HOST;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_PASSWORD;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.util.HashMap;
import java.util.Map;

import org.jose4j.json.JsonUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.ContextPathHolder;
import no.nav.vedtak.sikkerhet.domene.IdTokenAndRefreshToken;

public class OpenAMHelperTest {

    @Rule
    public LogSniffer logSniffer = new LogSniffer(Level.DEBUG);
    private OpenAMHelper helper;

    private String rpUsername;
    private String rpPassword;
    private String systembrukerUsername;
    private String systembrukerPassword;

    private static Level ORG_HTTP_CLIENT_LOG_LEVEL;
    private static Logger HTTP_CLIENT_LOGGER;

    private static void setProperty(String key, String value) {
        if (value != null) {
            System.setProperty(key, value);
        } else {
            System.clearProperty(key);
        }
    }

    @BeforeClass
    public static void ensureFrameworkLogging() {
        HTTP_CLIENT_LOGGER = (Logger) LoggerFactory.getLogger("org.apache.http.client");
        ORG_HTTP_CLIENT_LOG_LEVEL = HTTP_CLIENT_LOGGER.getLevel();
        HTTP_CLIENT_LOGGER.setLevel(Level.WARN);
    }

    @Before
    public void setUp() {
        OpenAMHelper.unsetWellKnownConfig();

        logSniffer.clearLog();

        System.setProperty(OPEN_ID_CONNECT_ISSO_HOST, "https://isso-t.adeo.no/isso/oauth2");
        System.setProperty(OPEN_ID_CONNECT_USERNAME, "fpsak-localhost");
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "http://app.expample.com");
        backupSystemProperties();
        ContextPathHolder.instance("/fpsak");
        helper = new OpenAMHelper();
    }

    @AfterClass
    public static void tearDownClass() {
        System.clearProperty(OPEN_ID_CONNECT_USERNAME);
        System.clearProperty(OPEN_ID_CONNECT_PASSWORD);
        System.clearProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL);
        HTTP_CLIENT_LOGGER.setLevel(ORG_HTTP_CLIENT_LOG_LEVEL);
    }

    @After
    public void tearDown() {
        restoreSystemProperties();
    }

    @Test(expected = IllegalArgumentException.class)
    public void skalFeileVedManglendeProperties() throws Exception {
        helper.getToken();
    }

    @Test
    public void skalReturnereGyldigTokenVedGyldigBrukernavnOgPassord() throws Exception {
        ignoreTestHvisPropertiesIkkeErsatt();

        IdTokenAndRefreshToken tokens = helper.getToken();

        assertThat(tokens.getIdToken()).isNotNull();
        assertThat(tokens.getRefreshToken()).isNotNull();
        logSniffer.assertHasWarnMessage("Cookie rejected");
        int entries = logSniffer.countEntries("F-050157:Uventet format for host");
        if (entries > 0) { // HACK (u139158): ServerInfo.cookieDomain beregnes kun en gang så når man kjører alle testene i modulen blir denne spist tidligere
            logSniffer.assertHasWarnMessage("F-050157:Uventet format for host");
        }
    }

    @Test
    public void skalFeilePåTokenVedFeilIRPUserMenGyldigBrukernavnOgPassord() throws Exception {
        ignoreTestHvisPropertiesIkkeErsatt();
        System.setProperty(OPEN_ID_CONNECT_USERNAME, ""); // Settes til ugyldig verdi slik at det vil feile på access_token.
        try {
            helper.getToken();
        } catch (TekniskException e) {
            assertThat(e.getMessage()).isEqualTo("F-909480:Fant ikke auth-code på responsen, får respons: '400 - Bad Request'");
            logSniffer.assertHasWarnMessage("Cookie rejected");
        }
    }

    @Test
    public void skalFåHTTP401FraOpenAM_VedUgyldigBrukernavnOgEllerPassord() throws Exception {
        ignoreTestHvisPropertiesIkkeErsatt();
        try {
            helper.getToken("NA", "NA");
        } catch (TekniskException e) {
            assertThat(e.getMessage()).startsWith("F-011609:Ikke-forventet respons fra OpenAm, statusCode 401");
            logSniffer.assertHasWarnMessage("Cookie rejected");
        }
    }

    @Test
    public void well_known_config_should_fail_gracefully() {
        try {
            System.setProperty(OPEN_ID_CONNECT_ISSO_HOST, "http://should.not.exist:23442/rest/isso/oauth2");
            String url = OpenAMHelper.getIssoIssuerUrl();
            assertThat(url).isNull();
        } catch (TekniskException e) {
            assertThat(e.getMessage()).startsWith(OpenAmFeil.SERVICE_DISCOVERY_FAILED_CODE);
        }
    }

    @Test
    public void should_be_able_to_read_from_well_known_config() {
        String expectedIssuer = "http://isso.example.com/rest/isso/oauth2";
        String expextedJwks = "http://isso.example.com/rest/isso/oauth2/connect/jwk_uri";
        String expectedAutorizationEndpoint = "http://isso.example.com/rest/isso/oauth2/authorize";
        Map<String, String> testData = new HashMap<>() {
            {
                put(OpenAMHelper.ISSUER_KEY, expectedIssuer);
                put(OpenAMHelper.JWKS_URI_KEY, expextedJwks);
                put(OpenAMHelper.AUTHORIZATION_ENDPOINT_KEY, expectedAutorizationEndpoint);
            }
        };
        OpenAMHelper.setWellKnownConfig(JsonUtil.toJson(testData));
        assertThat(OpenAMHelper.getIssoIssuerUrl()).isEqualTo(expectedIssuer);
        assertThat(OpenAMHelper.getIssoJwksUrl()).isEqualTo(expextedJwks);
        assertThat(OpenAMHelper.getAuthorizationEndpoint()).isEqualTo(expectedAutorizationEndpoint);
    }

    private void ignoreTestHvisPropertiesIkkeErsatt() {
        assumeTrue("RP-bruker: Brukernavn må være satt som VM-property", erSatt(OPEN_ID_CONNECT_USERNAME));
        assumeTrue("RP-bruker: Passord må være satt som VM-property", erSatt(OPEN_ID_CONNECT_PASSWORD));
    }

    private boolean erSatt(String key) {
        String property = System.getProperty(key);
        return property != null && !property.isEmpty();
    }

    private void backupSystemProperties() {
        rpUsername = System.getProperty(OPEN_ID_CONNECT_USERNAME);
        rpPassword = System.getProperty(OPEN_ID_CONNECT_PASSWORD);
    }

    private void restoreSystemProperties() {
        setProperty(OPEN_ID_CONNECT_USERNAME, rpUsername);
        setProperty(OPEN_ID_CONNECT_PASSWORD, rpPassword);
    }

}
