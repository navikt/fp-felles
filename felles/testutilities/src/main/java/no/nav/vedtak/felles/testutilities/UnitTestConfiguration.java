package no.nav.vedtak.felles.testutilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oppsett av konfigurasjon spesielt for enhetstester (inklusiv JUnit Integration Tests)
 */
public class UnitTestConfiguration {

    private static final Logger log = LoggerFactory.getLogger(UnitTestConfiguration.class);
    private static final String UNITTEST_PROPS = "./src/test/unittest.properties"; //$NON-NLS-1$

    public static void loadUnitTestProperties() {
        Properties properties = getUnitTestProperties();
        if (properties == null) {
            // ingenting nytt
            return;
        }

        loadToSystemProperties(properties, true);
    }

    /**
     * overstyrer eksisterende props, skriver ut underveis.
     */
    public static void loadToSystemProperties(Properties properties, boolean overwriteSystemProperties) {
        Properties systemProperties = System.getProperties();
        for (Entry<Object, Object> entry : properties.entrySet()) {
            if (overwriteSystemProperties || !systemProperties.containsKey(entry.getKey())) {
                log.info(entry.getKey() + " = " + entry.getValue()); //$NON-NLS-1$
                systemProperties.setProperty((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    public static Properties getUnitTestProperties() {
        return getUnitTestProperties(UNITTEST_PROPS);
    }

    public static Properties getUnitTestProperties(String unittestPropsFile) {
        File props = findUnitTestConfiguration(unittestPropsFile);
        return getUnitTestProperties(props == null ? null : props.toURI());
    }

    public static Properties getUnitTestProperties(URI uri) {
        Properties unitTestProps = new Properties();
        if (uri == null) {
            return unitTestProps;
        }
        try {
            URLConnection conn = uri.toURL().openConnection();
            try (InputStream is = conn.getInputStream()) {
                unitTestProps.load(is);
                return unitTestProps;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke laste props fra URI=" + uri); //$NON-NLS-1$
        }

    }

    private static File findUnitTestConfiguration(String devPath) {
        File baseDir = new File(".").getAbsoluteFile(); //$NON-NLS-1$
        File location = new File(baseDir, devPath);
        while (!location.exists()) {
            baseDir = baseDir.getParentFile();
            if (baseDir == null || !baseDir.isDirectory()) {
                return null;
            }
            location = new File(baseDir, devPath);
        }
        return location;
    }

}
