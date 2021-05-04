package no.nav.foreldrepenger.konfig;

import static java.lang.System.getenv;
import static no.nav.foreldrepenger.konfig.StandardPropertySource.APP_PROPERTIES;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.Dependent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi.Converter;

@Dependent
public class ApplicationPropertiesKonfigProvider extends PropertiesKonfigVerdiProvider {

    static class Init {
        // lazy init singleton
        static final Properties PROPS = lesFra();
        private static final String SUFFIX = ".properties";
        private static final String PREFIX = "application";

        private Init() {
        }

        private static Properties lesFra() {
            var c = new Properties();
            lesFra(namespaceKonfig(), lesFra(clusterKonfig(), lesFra("", new Properties())))
                    .forEach((k, v) -> c.put(k.toString().toLowerCase(), v.toString().toLowerCase()));
            return c;
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
            var namespaceName = namespaceName();
            if (namespaceName != null) {
                return clusterKonfig() + "-" + namespaceName;
            }
            return null;
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
    private static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";
    private static final String NAIS_NAMESPACE_NAME = "NAIS_NAMESPACE";

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationPropertiesKonfigProvider.class);

    public ApplicationPropertiesKonfigProvider() {
        super(Init.PROPS, APP_PROPERTIES);
    }

    @Override
    public <V> V getVerdi(String key, Converter<V> converter) {
        return Optional.ofNullable(super.getVerdi(key.toLowerCase(), converter))
                .orElse(null);
    }

    @Override
    public boolean harVerdi(String key) {
        return super.harVerdi(key.toLowerCase());
    }

    @Override
    public int getPrioritet() {
        return PRIORITET;
    }
}
