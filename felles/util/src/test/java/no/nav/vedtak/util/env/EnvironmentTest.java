package no.nav.vedtak.util.env;

import static no.nav.vedtak.util.env.Cluster.PROD_FSS;
import static no.nav.vedtak.util.env.ConfidentialMarkerFilter.CONFIDENTIAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.Tid;

public class EnvironmentTest {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentTest.class);
    private static final Environment ENV = Environment.current();

    @Test
    // Denne testen må kjøres fra maven, ettersom vi ikke enkelt kan sette env
    // properties i kode.
    public void testEnvironment() {
        assertEquals(ENV.getCluster(), PROD_FSS);
        assertEquals("jalla", ENV.namespace());
        assertTrue(ENV.isProd());
    }

    @Test
    public void testTurboFilterMedMarkerIProd() {
        var stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
        LOG.info(CONFIDENTIAL, "Dette er konfidensielt, OK i dev men ikke i prod, denne testen kjører i pseudo-prod");
        assertEquals(0, stdout.size());
    }

    @Test
    public void testTurboFilterUtenMarkerIProd() {
        var stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
        LOG.info("Dette er ikke konfidensielt");
        assertTrue(stdout.size() > 0);
    }

    @Test
    public void testDuration() {
        assertEquals(Duration.ofDays(42), ENV.getProperty("duration.property", Duration.class));
        assertEquals(Duration.ofDays(2), ENV.getProperty("ikkw.funnet", Duration.class, Duration.ofDays(2)));
    }

    @Test
    public void testString() {
        assertEquals("42", ENV.getProperty("finnes.ikke", "42"));
        assertNull(ENV.getProperty("finnes.ikke"));
    }

    @Test
    public void testBoolean() {
        assertTrue(ENV.getProperty("test4.boolean", boolean.class));
        assertTrue(ENV.getProperty("test4.boolean", Boolean.class));
    }

    @Test
    public void testInt() {
        assertEquals(Integer.valueOf(10), ENV.getProperty("test2.property", Integer.class));
        assertEquals(Integer.valueOf(10), ENV.getProperty("test2.property", int.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPropertiesFraEnvUkjentConverter() {
        ENV.getProperty("finnes.ikke", Tid.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testPropertiesIkkeFunnet() {
        ENV.getRequiredProperty("finnes.ikke");
    }
}
