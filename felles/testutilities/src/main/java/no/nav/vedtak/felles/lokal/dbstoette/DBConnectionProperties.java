package no.nav.vedtak.felles.lokal.dbstoette;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Enkel representasjon av properties for migrering av skjema med flyway.
 * Tilhørende json ser ca slik ut:
 * <p>
 *
 * <pre>
 * {
 *  "datasource" : "spsak",
 *  "schema": "spsak",
 *  "url": "jdbc:postgresql://localhost:5432/spsak",
 *  "migrationScriptsClasspathRoot": "database/migration/defaultDS",
 *  "migrateClean": true
 * }
 * </pre>
 * </p>
 * <p>
 * testdataClasspathRoot: pathen til java-klasser for testdata<br>
 * migrationScriptsFilesystemRoot: filsystemsti hvor migreringsfilene for angitt
 * skjema ligger<br>
 * migrationScriptsClasspathRoot: classpath sti hvor migreringsfilene for angitt
 * skjema ligger<br>
 * defaultDataSource: får JDNI-oppslag som 'java/defaultDS' hvis satt til true
 * (default false)<br>
 * migrateClean: fullmigrering av skjema (default false)<br>
 * </p>
 *
 * @deprecated Dette kan nå kodes direkte i apps som benytter istedt for å lese
 *             fra json
 */
@Deprecated(forRemoval = true, since = "2.3.x")
public final class DBConnectionProperties {

    private static final ObjectMapper OM = new ObjectMapper();

    private String datasource;
    private String schema;
    private String defaultSchema;
    private String url;
    private String user;
    private String password;

    private String migrationScriptsFilesystemRoot;
    private String migrationScriptsClasspathRoot;

    private String versjonstabell;
    private boolean defaultDataSource;
    private boolean migrateClean;

    private DBConnectionProperties() {
    }

    private DBConnectionProperties(Builder builder) {
        this.datasource = builder.datasource;
        this.schema = builder.schema;
        this.defaultSchema = builder.defaultSchema;
        this.url = builder.url;
        this.user = builder.user;
        this.password = builder.password;
        this.migrationScriptsFilesystemRoot = builder.migrationScriptsFilesystemRoot;
        this.migrationScriptsClasspathRoot = builder.migrationScriptsClasspathRoot;
        this.versjonstabell = builder.versjonstabell;
        this.defaultDataSource = builder.defaultDataSource;
        this.migrateClean = builder.migrateClean;
    }

    public static List<DBConnectionProperties> fraFil(File jsonFil) throws IOException {
        try (InputStream is = new FileInputStream(jsonFil)) {
            return fraStream(is);
        }
    }

    public static List<DBConnectionProperties> fraStream(InputStream jsonFil) {
        List<DBConnectionProperties> dbProperties = new ArrayList<>();

        try {
            JsonNode json = OM.reader().readTree(jsonFil);
            for (var sc : json.get("schemas")) {
                dbProperties.add(read(sc));
            }
        } catch (IOException e) {

        }

        if (dbProperties.stream().filter(DBConnectionProperties::isDefaultDataSource).count() > 1L) {
            throw new IllegalStateException("Kun en dataSource kan være default");
        }

        return dbProperties;
    }

    public static List<DBConnectionProperties> rawFraStream(InputStream jsonFil) {
        List<DBConnectionProperties> dbProperties = new ArrayList<>();

        try {
            JsonNode json = OM.reader().readTree(jsonFil);
            for (var sc : json.get("schemas")) {
                dbProperties.add(readRaw(sc));
            }
        } catch (IOException e) {

        }

        if (dbProperties.stream().filter(DBConnectionProperties::isDefaultDataSource).count() > 1L) {
            throw new IllegalStateException("Kun en dataSource kan være default");
        }

        return dbProperties;
    }

    public static Optional<DBConnectionProperties> finnDefault(List<DBConnectionProperties> connectionProperties) {
        return connectionProperties.stream().filter(DBConnectionProperties::isDefaultDataSource).findFirst();
    }

    private static DBConnectionProperties read(JsonNode db) {
        return readRaw(db);
    }

    private static DBConnectionProperties readRaw(JsonNode sc) {

        final String datasource = getString(sc, "datasource");
        final String schema = getString(sc, "schema");
        final String defaultSchema = getString(sc, "defaultSchema");
        final String user = getString(sc, "user", schema);
        final String password = getString(sc, "password", schema);
        final String migrationScriptsClasspathRoot = getString(sc, "migrationScriptsClasspathRoot");
        final String migrationScriptsFilesystemRoot = getString(sc, "migrationScriptsFilesystemRoot");
        final String tabell = getString(sc, "versjonstabell", "schema_version");
        final String url = getString(sc, "url");

        boolean isDefaultDataSource = Boolean.valueOf(getString(sc, "default"));
        boolean migrateClean2 = Boolean.valueOf(getString(sc, "migrateClean"));

        return new Builder()
                .datasource(datasource)
                .schema(schema)
                .defaultSchema(defaultSchema)
                .user(user)
                .password(password)
                .migrationScriptsClasspathRoot(migrationScriptsClasspathRoot)
                .migrationScriptsFilesystemRoot(migrationScriptsFilesystemRoot)
                .versjonstabell(tabell)
                .url(url)
                .defaultDataSource(isDefaultDataSource)
                .migrateClean(migrateClean2)
                .build();
    }

    private static String getString(JsonNode jo, String key, String defaultValue) {
        var val = getString(jo, key);
        return val != null ? val : defaultValue;
    }

    private static String getString(JsonNode sc, String key) {
        // fordi glassfish impl sux sjekker vi for nulls først slik at ikke det spruter
        // interne NPE is JsonObject
        var val = sc.get(key);
        return val == null ? null : val.asText();
    }

    public String getDatasource() {
        return datasource;
    }

    public String getSchema() {
        return schema;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getMigrationScriptsFilesystemRoot() {
        return migrationScriptsFilesystemRoot;
    }

    public String getMigrationScriptsClasspathRoot() {
        return migrationScriptsClasspathRoot;
    }

    public String getVersjonstabell() {
        return versjonstabell;
    }

    public boolean isDefaultDataSource() {
        return defaultDataSource;
    }

    public boolean isMigrateClean() {
        return migrateClean;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public static class Builder {
        private String datasource;
        private String schema;
        private String defaultSchema;
        private String url;
        private String user;
        private String password;
        private String migrationScriptsFilesystemRoot;
        private String migrationScriptsClasspathRoot;
        private String versjonstabell;
        private boolean defaultDataSource;
        private boolean migrateClean;

        public Builder datasource(String datasource) {
            this.datasource = datasource;
            return this;
        }

        public Builder schema(String schema) {
            this.schema = schema;
            return this;
        }

        public Builder defaultSchema(String defaultSchema) {
            this.defaultSchema = defaultSchema;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder migrationScriptsFilesystemRoot(String migrationScriptsFilesystemRoot) {
            this.migrationScriptsFilesystemRoot = migrationScriptsFilesystemRoot;
            return this;
        }

        public Builder migrationScriptsClasspathRoot(String migrationScriptsClasspathRoot) {
            this.migrationScriptsClasspathRoot = migrationScriptsClasspathRoot;
            return this;
        }

        public Builder versjonstabell(String versjonstabell) {
            this.versjonstabell = versjonstabell;
            return this;
        }

        public Builder defaultDataSource(boolean defaultDataSource) {
            this.defaultDataSource = defaultDataSource;
            return this;
        }

        public Builder migrateClean(boolean migrateClean) {
            this.migrateClean = migrateClean;
            return this;
        }

        public Builder fromPrototype(DBConnectionProperties prototype) {
            datasource = prototype.datasource;
            schema = prototype.schema;
            defaultSchema = prototype.defaultSchema;
            url = prototype.url;
            user = prototype.user;
            password = prototype.password;
            migrationScriptsFilesystemRoot = prototype.migrationScriptsFilesystemRoot;
            migrationScriptsClasspathRoot = prototype.migrationScriptsClasspathRoot;
            versjonstabell = prototype.versjonstabell;
            defaultDataSource = prototype.defaultDataSource;
            migrateClean = prototype.migrateClean;
            return this;
        }

        public DBConnectionProperties build() {
            return new DBConnectionProperties(this);
        }
    }
}
