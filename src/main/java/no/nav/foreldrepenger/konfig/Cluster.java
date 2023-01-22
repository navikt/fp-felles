package no.nav.foreldrepenger.konfig;

import static java.lang.System.getenv;

import java.util.Arrays;
import java.util.Objects;

public enum Cluster {
    LOCAL("local"),
    VTP("vtp"),
    DEV_FSS("dev-fss"),
    DEV_GCP("dev-gcp"),
    PROD_GCP("prod-gcp"),
    PROD_FSS("prod-fss");

    private static final String PROD = "prod";
    private static final String DEV = "dev";

    private final String name;

    Cluster(String name) {
        this.name = name;
    }

    public String clusterName() {
        return name;
    }

    public boolean isProd() {
        return name.startsWith(PROD);
    }

    public boolean isDev() {
        return name.startsWith(DEV);
    }

    boolean isVTP() {
        return name.startsWith(VTP.name);
    }

    public boolean isLocal() {
        return !isProd() && !isDev();
    }

    public static Cluster current() {
        var active = getenv(NaisProperty.CLUSTER.propertyName());
        return Arrays.stream(values())
                .filter(c -> active != null && Objects.equals(active, c.name))
                .findFirst()
                .orElse(LOCAL);
    }

    public static Cluster of(String name) {
        return Arrays.stream(values())
                .filter(v -> v.name.equals(name))
                .findFirst()
                .orElseThrow();
    }

}
