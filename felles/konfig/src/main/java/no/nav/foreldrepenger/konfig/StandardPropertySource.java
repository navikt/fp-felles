package no.nav.foreldrepenger.konfig;

public enum StandardPropertySource {
    SYSTEM_PROPERTIES("System properties"),
    ENV_PROPERTIES("Environment properties"),
    APP_PROPERTIES("Application properties"),
    APP_PROPERTIES_CLUSTER("Cluster-specific application properties"),
    APP_PROPERTIES_CLUSTER_NAMESPACE("Cluster og namespace specific application properties"),

    DEFAULT("Default");

    private final String name;

    StandardPropertySource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
