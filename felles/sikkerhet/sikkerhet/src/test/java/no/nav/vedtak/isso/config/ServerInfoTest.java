package no.nav.vedtak.isso.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import no.nav.vedtak.log.util.MemoryAppender;
import no.nav.vedtak.sikkerhet.ContextPathHolder;

public class ServerInfoTest {

    private static MemoryAppender logSniffer;
    private static Logger LOG;

    @BeforeAll
    public static void beforeAll() {
        LOG = Logger.class.cast(LoggerFactory.getLogger(ServerInfo.class));
        LOG.setLevel(Level.INFO);
        logSniffer = new MemoryAppender(LOG.getName());
    }

    @BeforeEach
    public void beforeEach() {
        logSniffer.reset();
        LOG.addAppender(logSniffer);
        logSniffer.start();
        ContextPathHolder.instance("/fpsak");

    }

    @AfterEach
    public void afterEach() {
        logSniffer.stop();
        LOG.detachAppender(logSniffer);
    }

    @Test
    public void skalGenerereGyldigCallbackURL() throws Exception {
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "http://localhost:8080");
        ServerInfo serverinfo = new ServerInfo();
        assertThat(serverinfo.getCallbackUrl()).isEqualTo("http://localhost:8080/fpsak/cb");
        System.clearProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL);
    }

    @Test
    public void skal_hente_cookie_domain_fra_loadbalancerUrl_og_utvide_ett_nivå() throws Exception {
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "https://bar.nav.no");
        assertThat(new ServerInfo().getCookieDomain()).isEqualTo("nav.no");
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "https://baz.devillo.no");
        assertThat(new ServerInfo().getCookieDomain()).isEqualTo("devillo.no");
        System.clearProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL);
    }

    @Test
    public void skal_sett_cookie_domain_til_null_når_domenet_er_for_smalt_til_å_utvides_og_logge_dette() throws Exception {
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "https://nav.no");
        assertThat(new ServerInfo().getCookieDomain()).isNull();
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "http://localhost:8080");
        assertThat(new ServerInfo().getCookieDomain()).isNull();
        System.clearProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL);
        assertThat(logSniffer.search("Uventet format for host", Level.WARN)).hasSize(2);
    }
}