package no.nav.foreldrepenger.konfig;

/**
 * Standard navn på environment injisert av NAIS
 * Med vilje ikke public slik at man heller går via Environment
 */
enum NaisProperty {
    CLUSTER("NAIS_CLUSTER_NAME"),
    NAMESPACE("NAIS_NAMESPACE"),
    APPLICATION("NAIS_APP_NAME"),
    CLIENTID("NAIS_CLIENT_ID"), // Format <cluster>:<namespace>:<application>
    IMAGE("NAIS_APP_IMAGE"),    // Format <path>/<app>:<date>-<git hash >
    ;

    private final String name;

    NaisProperty(String name) {
        this.name = name;
    }

    String propertyName() {
        return name;
    }

}
