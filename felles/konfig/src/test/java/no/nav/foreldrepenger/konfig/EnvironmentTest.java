package no.nav.foreldrepenger.konfig;

import static no.nav.foreldrepenger.konfig.Cluster.PROD_FSS;
import static no.nav.foreldrepenger.konfig.Cluster.PROD_GCP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintStream;
import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EnvironmentTest {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentTest.class);
    private static final Environment ENV = Environment.current();
    private static final PrintStream SYSOUT = System.out;

    @AfterEach
    void after() {
        System.setOut(SYSOUT);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "mvn", matches = "true")
    void testEnvironment() {
        assertEquals(PROD_FSS, ENV.getCluster());
        assertEquals("default", ENV.namespace());
        assertTrue(ENV.isProd());
    }

    @Test
    void testCoLocated() {
        assertTrue(Cluster.VTP.isCoLocated(Cluster.VTP));
        assertTrue(Cluster.DEV_GCP.isCoLocated(Cluster.DEV_GCP));
        assertTrue(PROD_FSS.isCoLocated(PROD_FSS));
        assertFalse(PROD_FSS.isCoLocated(Cluster.VTP));
        assertFalse(PROD_FSS.isCoLocated(Cluster.PROD_GCP));
    }

    @Test
    void testClass() {
        assertTrue(Cluster.VTP.isSameClass(Cluster.VTP));
        assertTrue(Cluster.DEV_GCP.isSameClass(Cluster.DEV_GCP));
        assertTrue(PROD_FSS.isSameClass(PROD_GCP));
        assertFalse(PROD_FSS.isSameClass(Cluster.VTP));
        assertFalse(Cluster.DEV_FSS.isSameClass(PROD_FSS));
    }

    @Test
    void testURI() {
        assertEquals(URI.create("http://www.nav.no"), ENV.getRequiredProperty("NAV", URI.class));
    }

    @Test
    void testUppercase() {
        assertEquals(PROD_FSS.clusterName(), ENV.getProperty("nais.cluster.name"));
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
        assertThrows(IllegalArgumentException.class, () -> ENV.getProperty("finnes.ikke", Double.class));
    }

    @Test
    void testPropertiesIkkeFunnet() {
        assertThrows(IllegalStateException.class, () -> ENV.getRequiredProperty("finnes.ikke"));
    }

}
