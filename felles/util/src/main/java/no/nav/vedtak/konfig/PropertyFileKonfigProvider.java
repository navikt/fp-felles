package no.nav.vedtak.konfig;

import static java.lang.System.getenv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PropertyFileKonfigProvider extends PropertiesKonfigVerdiProvider {

    private static final String LOCAL = "local";
    private static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";
    private static final String SUFFIX = ".properties";
    private static final String PREFIX = "application";
    private static final Logger LOG = LoggerFactory.getLogger(PropertyFileKonfigProvider.class);

    protected PropertyFileKonfigProvider() {
        super(properties(PREFIX));
    }

    private static Properties properties(String prefix) {
        return properties(prefix + "-" + cluster(), properties(prefix, new Properties()));
    }

    private static String cluster() {
        return Optional.ofNullable(getenv(NAIS_CLUSTER_NAME))
                .orElse(LOCAL);
    }

    private static Properties properties(String prefix, Properties p) {
        String navn = prefix + SUFFIX;
        try (InputStream is = PropertyFileKonfigProvider.class.getClassLoader().getResourceAsStream(navn)) {
            if (is != null) {
                LOG.info("Laster properties fra {}", navn);
                p.load(is);
            }
        } catch (IOException e) {
            LOG.info("Propertyfil {} ikke funnet", navn);
        }
        return p;
    }

    @Override
    public int getPrioritet() {
        return 12;
    }
}
