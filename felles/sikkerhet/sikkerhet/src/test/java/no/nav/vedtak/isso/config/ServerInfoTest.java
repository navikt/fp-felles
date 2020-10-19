package no.nav.vedtak.isso.config;

import static no.nav.vedtak.isso.config.ServerInfo.PROPERTY_KEY_LOADBALANCER_URL;
import static no.nav.vedtak.log.util.MemoryAppender.sniff;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertEquals("http://localhost:8080/fpsak/cb", serverinfo.getCallbackUrl());
        System.clearProperty(PROPERTY_KEY_LOADBALANCER_URL);
    }

    @Test
    public void skal_hente_cookie_domain_fra_loadbalancerUrl_og_utvide_ett_nivå() throws Exception {
        System.setProperty(PROPERTY_KEY_LOADBALANCER_URL, "https://bar.nav.no");
        assertEquals("nav.no", new ServerInfo().getCookieDomain());
        System.setProperty(PROPERTY_KEY_LOADBALANCER_URL, "https://baz.devillo.no");
        assertEquals("devillo.no", new ServerInfo().getCookieDomain());
        System.clearProperty(PROPERTY_KEY_LOADBALANCER_URL);
    }

    @Test
    public void skal_sett_cookie_domain_til_null_når_domenet_er_for_smalt_til_å_utvides_og_logge_dette() throws Exception {
        System.setProperty(PROPERTY_KEY_LOADBALANCER_URL, "https://nav.no");
        assertNull(new ServerInfo().getCookieDomain());
        System.setProperty(PROPERTY_KEY_LOADBALANCER_URL, "http://localhost:8080");
        assertNull(new ServerInfo().getCookieDomain());
        System.clearProperty(PROPERTY_KEY_LOADBALANCER_URL);
    }
}