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

import no.nav.vedtak.konfig.StandardPropertySource;
import no.nav.vedtak.konfig.Tid;

public class EnvironmentTest {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentTest.class);
    private static Environment ENV = Environment.current();
    private static PrintStream SYSOUT = System.out;
    
    @org.junit.After
    public void after() throws Exception {
        // reset
        System.setOut(SYSOUT);
    }

    @Test
    // Denne testen må kjøres fra maven, ettersom vi ikke enkelt kan sette env
    // properties i kode.
    public void testEnvironment() {
        assertEquals(ENV.getCluster(), PROD_FSS);
        assertEquals("jalla", ENV.namespace());
        assertTrue(ENV.isProd());
    }

    public void testUppercase() {
        assertEquals(PROD_FSS.clusterName(), ENV.getProperty("nais.cluster.name"));
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
        assertEquals("duration.property", Duration.ofDays(42), ENV.getProperty("duration.property", Duration.class));
        assertEquals("ikke.funnet", Duration.ofDays(2),
                ENV.getProperty("ikke.funnet", Duration.class, Duration.ofDays(2)));
    }

    @Test
    public void testString() {
        assertEquals("finnes.ikke", "42", ENV.getProperty("finnes.ikke", "42"));
        assertNull(ENV.getProperty("finnes.ikke"));
    }

    @Test
    public void testBoolean() {
        assertTrue("test4.boolean", ENV.getProperty("test4.boolean", boolean.class));
        assertTrue("test4.boolean", ENV.getProperty("test4.boolean", Boolean.class));
    }

    @Test
    public void testInt() {
        LOG.info("Application property verdier {}", ENV.getProperties(StandardPropertySource.APP_PROPERTIES));
        assertEquals("test2.intproperty", Integer.valueOf(10), ENV.getProperty("test2.intproperty", Integer.class));
        assertEquals("test2.intproperty", Integer.valueOf(10), ENV.getProperty("test2.intproperty", int.class));
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
