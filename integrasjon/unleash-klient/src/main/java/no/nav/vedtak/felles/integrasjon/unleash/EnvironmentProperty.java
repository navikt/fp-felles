package no.nav.vedtak.felles.integrasjon.unleash;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.PropertyUtil;

public class EnvironmentProperty {

    public static final String NAIS_NAMESPACE = "NAIS_NAMESPACE_NAME";
    public static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";
    public static final List<String> PROD_CLUSTERS = List.of("prod-fss", "prod-sbs");
    public static final List<String> PREPROD_CLUSTERS = List.of("dev-fss", "dev-sbs");
    public static final String PROD = "p";
    public static final String PREPROD = "q";
    static final String APP_NAME = "NAIS_APP_NAME";
    private static final String FASIT_ENVIRONMENT_NAME = "FASIT_ENVIRONMENT_NAME";
    private static final Logger log = LoggerFactory.getLogger(EnvironmentProperty.class);
    public static final String DEFAULT_NAMESPACE = "default";

    private EnvironmentProperty() {
    }

    public static Optional<String> getEnvironmentName() {
        String environmentName = PropertyUtil.getProperty(FASIT_ENVIRONMENT_NAME);
        if (environmentName != null && !environmentName.isEmpty()) {
            return Optional.of(environmentName);
        }
        environmentName = PropertyUtil.getProperty(NAIS_NAMESPACE);
        if (environmentName != null && !environmentName.isEmpty() && !environmentName.equals(DEFAULT_NAMESPACE)) {
            log.info("{} ikke satt setter environmentName={}", FASIT_ENVIRONMENT_NAME, environmentName);
            return Optional.of(environmentName);
        }
        String cluster = PropertyUtil.getProperty(NAIS_CLUSTER_NAME);
        if (cluster != null && !cluster.isEmpty() && PROD_CLUSTERS.contains(cluster.toLowerCase())) {
            return Optional.of(PROD);
        }

        if(cluster != null && !cluster.isEmpty() && PREPROD_CLUSTERS.contains(cluster.toLowerCase())) {
            if (environmentName != null && !environmentName.isEmpty() && environmentName.equals(DEFAULT_NAMESPACE)) {
                return Optional.of(PREPROD);
            }
        }

        String property = PropertyUtil.getProperty("environment.name");
        log.info("{} ikke satt setter environmentName={}", NAIS_NAMESPACE, property);
        return Optional.ofNullable(property);
    }

    static Optional<String> getAppName() {
        String appName = PropertyUtil.getProperty(APP_NAME);
        if (appName == null) {
            appName = PropertyUtil.getProperty("application.name");
        }
        return Optional.ofNullable(appName);
    }
}
