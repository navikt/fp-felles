package no.nav.vedtak.konfig;

import static java.lang.System.getenv;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PropertyFileKonfigProvider extends PropertiesKonfigVerdiProvider {

    private static final int PRIORITET = EnvPropertiesKonfigVerdiProvider.PRIORITET + 1;
    private static final String LOCAL = "local";
    private static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";
    private static final String NAIS_NAMESPACE_NAME = "NAIS_NAMESPACE";

    private static final String SUFFIX = ".properties";
    private static final String PREFIX = "application";
    private static final Logger LOG = LoggerFactory.getLogger(PropertyFileKonfigProvider.class);

    public PropertyFileKonfigProvider() {
        super(lesFra());
    }

    private static Properties lesFra() {
        return lesFra(namespaceKonfig(), lesFra(clusterKonfig(), lesFra("", new Properties())));
    }

    private static Properties lesFra(String infix, Properties p) {
        if (infix == null) {
            LOG.info("Ingen namespace-spesifikk konfigurasjon funnet");
            return p;
        }
        String navn = PREFIX + infix + SUFFIX;
        try (var is = PropertyFileKonfigProvider.class.getClassLoader().getResourceAsStream(navn)) {
            if (is != null) {
                LOG.info("Laster properties fra {}", navn);
                p.load(is);
                return p;
            }
        } catch (IOException e) {
            LOG.info("Propertyfil {} ikke lesbar", navn);
        }
        LOG.info("Propertyfil {} ikke funnet", navn);
        return p;
    }

    @Override
    public int getPrioritet() {
        return PRIORITET;
    }

    private static String clusterKonfig() {
        return "-" + clusterName();
    }

    private static String namespaceKonfig() {
        var namespaceName = namespaceName();
        if (namespaceName != null) {
            return clusterKonfig() + "-" + namespaceName;
        }
        return null;
    }

    private static String clusterName() {
        return Optional.ofNullable(getenv(NAIS_CLUSTER_NAME))
                .orElse(LOCAL);
    }

    private static String namespaceName() {
        return Optional.ofNullable(getenv(NAIS_NAMESPACE_NAME))
                .orElse(null);
    }
}
