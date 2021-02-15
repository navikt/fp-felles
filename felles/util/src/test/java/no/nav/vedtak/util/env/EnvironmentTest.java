package no.nav.vedtak.util.env;

import static no.nav.vedtak.util.env.Cluster.PROD_FSS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.StandardPropertySource;
import no.nav.vedtak.konfig.Tid;

class EnvironmentTest {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentTest.class);
    private static Environment ENV = Environment.current();
    private static PrintStream SYSOUT = System.out;

    @AfterEach
    void after() throws Exception {
        System.setOut(SYSOUT);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "mvn", matches = "true")
    void testEnvironment() {
        assertEquals(PROD_FSS, ENV.getCluster());
        assertEquals("jalla", ENV.namespace());
        assertTrue(ENV.isProd());
    }

    void testURI() {
        assertEquals(ENV.getRequiredProperty("VG", URI.class), URI.create("http://www.vg.no"));
    }

    void testUppercase() {
        assertEquals(PROD_FSS.clusterName(), ENV.getProperty("nais.cluster.name"));
    }

    @Test
    void testTurboFilterUtenMarkerIProd() {
        var stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
        LOG.info("Dette er ikke konfidensielt");
        assertTrue(stdout.size() > 0);
    }

    @Test
    void testDuration() {
        assertEquals(Duration.ofDays(42), ENV.getProperty("duration.property", Duration.class));
        assertEquals(Duration.ofDays(2),
                ENV.getProperty("ikke.funnet", Duration.class, Duration.ofDays(2)));
    }

    @Test
    void testString() {
        assertEquals("42", ENV.getProperty("finnes.ikke", "42"));
        assertNull(ENV.getProperty("finnes.ikke"));
    }

    @Test
    void testBoolean() {
        assertTrue(ENV.getProperty("test4.boolean", boolean.class));
        assertTrue(ENV.getProperty("test4.boolean", Boolean.class));
    }

    @Test
    void testInt() {
        LOG.info("Application property verdier {}", ENV.getProperties(StandardPropertySource.APP_PROPERTIES));
        assertEquals(Integer.valueOf(10), ENV.getProperty("test2.intproperty", Integer.class));
        assertEquals(Integer.valueOf(10), ENV.getProperty("test2.intproperty", int.class));
    }

    @Test
    void testPropertiesFraEnvUkjentConverter() {
        assertThrows(IllegalArgumentException.class, () -> ENV.getProperty("finnes.ikke", Tid.class));
    }

    @Test
    void testPropertiesIkkeFunnet() {
        assertThrows(IllegalStateException.class, () -> ENV.getRequiredProperty("finnes.ikke"));
    }

}
