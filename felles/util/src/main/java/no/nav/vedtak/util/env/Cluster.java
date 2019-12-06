package no.nav.vedtak.util.env;

import static java.lang.System.getenv;

import java.util.Arrays;
import java.util.Optional;

public enum Cluster {
    LOCAL("local", false),
    DEV_FSS("dev-fss", false),
    DEV_SBS("dev-sbs", false),
    DEV_GCP("dev-gcp", false),
    PROD_SBS("prod-sbs", true),
    PROD_GCP("prod-gcp", true),
    PROD_FSS("prod-fss", true);

    public static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";

    private final String name;
    private final boolean isProd;

    Cluster(String name, boolean isProd) {
        this.name = name;
        this.isProd = isProd;
    }

    public String clusterName() {
        return name;
    }

    public boolean isProd() {
        return isProd;
    }

    public static Cluster current() {
        return Arrays.stream(values())
                .filter(Cluster::isActive)
                .findFirst()
                .orElse(LOCAL);
    }

    private boolean isActive() {
        return Optional.ofNullable(getenv(NAIS_CLUSTER_NAME))
                .filter(name::equals)
                .isPresent();
    }
}
