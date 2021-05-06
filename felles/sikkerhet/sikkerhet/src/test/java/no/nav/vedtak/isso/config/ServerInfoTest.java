package no.nav.vedtak.isso.config;

import static no.nav.vedtak.isso.config.ServerInfo.PROPERTY_KEY_LOADBALANCER_URL;
import static no.nav.vedtak.log.util.MemoryAppender.sniff;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import no.nav.vedtak.log.util.MemoryAppender;
import no.nav.vedtak.sikkerhet.ContextPathHolder;

public class ServerInfoTest {

    private static MemoryAppender logSniffer;

    @BeforeAll
    public static void beforeAll() {
        logSniffer = sniff(ServerInfo.class);

    }

    @BeforeEach
    public void beforeEach() {
        ContextPathHolder.instance("/fpsak");
    }

    @AfterEach
    public void afterEach() {
        logSniffer.reset();
    }

    @Test
    public void skalGenerereGyldigCallbackURL() throws Exception {
        System.setProperty(PROPERTY_KEY_LOADBALANCER_URL, "http://localhost:8080");
        ServerInfo serverinfo = new ServerInfo();
        assertThat(serverinfo.getCallbackUrl()).isEqualTo("http://localhost:8080/fpsak/cb");
        System.clearProperty(PROPERTY_KEY_LOADBALANCER_URL);
    }

    @Test
    public void skal_hente_cookie_domain_fra_loadbalancerUrl_og_utvide_ett_nivå() throws Exception {
        System.setProperty(PROPERTY_KEY_LOADBALANCER_URL, "https://bar.nav.no");
        assertThat(new ServerInfo().getCookieDomain()).isEqualTo("nav.no");
        System.setProperty(PROPERTY_KEY_LOADBALANCER_URL, "https://baz.devillo.no");
        assertThat(new ServerInfo().getCookieDomain()).isEqualTo("devillo.no");
        System.clearProperty(PROPERTY_KEY_LOADBALANCER_URL);
    }

    @Test
    public void skal_sett_cookie_domain_til_null_når_domenet_er_for_smalt_til_å_utvides_og_logge_dette() throws Exception {
        System.setProperty(PROPERTY_KEY_LOADBALANCER_URL, "https://nav.no");
        assertThat(new ServerInfo().getCookieDomain()).isNull();
        System.setProperty(PROPERTY_KEY_LOADBALANCER_URL, "http://localhost:8080");
        assertThat(new ServerInfo().getCookieDomain()).isNull();
        System.clearProperty(PROPERTY_KEY_LOADBALANCER_URL);
        assertThat(logSniffer.search("Uventet format for host", Level.WARN)).hasSize(2);
    }
}