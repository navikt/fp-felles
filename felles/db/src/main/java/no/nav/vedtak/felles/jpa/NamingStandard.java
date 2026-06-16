package no.nav.vedtak.felles.jpa;

public class NamingStandard {

    private NamingStandard() {
    }

    // Standard name in persistence xml
    public static final String DEFAULT_PERSISTENCE_UNIT = "pu-default";

    // Standard data source name, and paths to migrations
    public static final String DEFAULT_DATA_SOURCE = "defaultDS";
    public static final String DEFAULT_MIGRATION_ROOT = "/db/migration/";
    public static final String DEFAULT_DS_MIGRATION_PATH = DEFAULT_MIGRATION_ROOT + DEFAULT_DATA_SOURCE;
    public static final String DEFAULT_DS_MIGRATION_CLASSPATH = "classpath:" + DEFAULT_MIGRATION_ROOT + DEFAULT_DATA_SOURCE;

}
