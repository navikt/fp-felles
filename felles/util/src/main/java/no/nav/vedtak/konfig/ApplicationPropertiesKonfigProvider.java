package no.nav.vedtak.konfig;

import static java.lang.System.getenv;
import static no.nav.foreldrepenger.konfig.Cluster.NAIS_CLUSTER_NAME;
import static no.nav.foreldrepenger.konfig.Namespace.NAIS_NAMESPACE_NAME;
import static no.nav.vedtak.konfig.StandardPropertySource.APP_PROPERTIES;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.Dependent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class ApplicationPropertiesKonfigProvider extends PropertiesKonfigVerdiProvider {

    static class Init {

        private Init() {

        }

        static final Properties PROPS = lesFra();
        private static final String SUFFIX = ".properties";
        private static final String PREFIX = "application";

        private static Properties lesFra() {
            return lesFra(namespaceKonfig(), lesFra(clusterKonfig(), lesFra("", new Properties())));
        }

        private static Properties lesFra(String infix, Properties p) {
            if (infix == null) {
                return p;
            }
            String navn = PREFIX + infix + SUFFIX;
            try (var is = ApplicationPropertiesKonfigProvider.class.getClassLoader().getResourceAsStream(navn)) {
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

        private static String namespaceKonfig() {
            return Optional.ofNullable(namespaceName())
                    .map(n -> clusterKonfig() + "-" + n)
                    .orElse(null);
        }

        private static String namespaceName() {
            return Optional.ofNullable(getenv(NAIS_NAMESPACE_NAME))
                    .orElse(null);
        }

        private static String clusterKonfig() {
            return "-" + clusterName();
        }

        private static String clusterName() {
            return Optional.ofNullable(getenv(NAIS_CLUSTER_NAME))
                    .orElse(LOCAL);
        }

    }

    private static final int PRIORITET = EnvPropertiesKonfigVerdiProvider.PRIORITET + 1;
    private static final String LOCAL = "local";

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationPropertiesKonfigProvider.class);

    public ApplicationPropertiesKonfigProvider() {
        super(Init.PROPS, APP_PROPERTIES);
    }

    @Override
    public int getPrioritet() {
        return PRIORITET;
    }
}
