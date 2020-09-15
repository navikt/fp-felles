package no.nav.vedtak.isso.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import no.nav.vedtak.sikkerhet.ContextPathHolder;

public class ServerInfoTest {

    @BeforeEach
    public void setUp() throws Exception {
        ContextPathHolder.instance("/fpsak");
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
        var listAppender = new ListAppender<ILoggingEvent>();
        listAppender.start();
        Logger.class.cast(LoggerFactory.getLogger(ServerInfo.class)).addAppender(listAppender);
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "https://nav.no");
        assertThat(new ServerInfo().getCookieDomain()).isNull();
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "http://localhost:8080");
        assertThat(new ServerInfo().getCookieDomain()).isNull();

        System.clearProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL);
        var meldinger = listAppender.list.stream().collect(Collectors.toMap(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel));
        assertEquals(2, meldinger.size());
        meldinger.entrySet().stream().forEach(e -> {
            assertEquals(Level.WARN, e.getValue());
            assertTrue(e.getKey().contains("Uventet format for host"));
        });

    }
}