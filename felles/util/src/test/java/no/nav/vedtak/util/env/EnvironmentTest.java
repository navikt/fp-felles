package no.nav.vedtak.util.env;

import static no.nav.vedtak.util.env.Cluster.PROD_FSS;
import static no.nav.vedtak.util.env.ConfidentialMarkerFilter.CONFIDENTIAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Denne testen må kjøres fra maven, ettersom vi ikke enkelt kan sette env properties i kode. 
public class EnvironmentTest {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentTest.class);
    private static final Environment ENV = Environment.current();

    @Test
    public void testEnvironment() {
        var stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
        LOG.info(CONFIDENTIAL, "Dette er konfidensielt, OK i dev men ikke i prod, denne testen kjører i pseudo-prod");
        assertEquals(ENV.clusterName(), PROD_FSS.clusterName());
        assertEquals("jalla", ENV.namespace());
        assertTrue(ENV.isProd());
        assertEquals(0, stdout.size());
    }
}
