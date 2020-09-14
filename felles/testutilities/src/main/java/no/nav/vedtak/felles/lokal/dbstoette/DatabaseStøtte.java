package no.nav.vedtak.felles.lokal.dbstoette;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.db.FlywayKonfig;

/**
 * Støtte for migrering av databaseskjema lokalt (brukes til jetty, enhetstester, og jenkins)
 * Setter opp JDNI-oppslag.
 * 
 * @deprecated Kjør heller Flyway direkte fra app som benytter
 */
@Deprecated(forRemoval = true)
public final class DatabaseStøtte {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseStøtte.class);

    private DatabaseStøtte() {
    }

    /**
     * Migrering kjøres i vilkårlig rekkefølge. Hvis bruker/skjema angitt i {@link DBConnectionProperties}
     * ikke finnes, opprettes den
     */
    public static void kjørMigreringFor(List<DBConnectionProperties> connectionProperties) {
        connectionProperties.forEach(DatabaseStøtte::kjørerMigreringFor);
    }

    /**
     * Setter JDNI-oppslag for default skjema
     */
    public static void settOppJndiForDefaultDataSource(List<DBConnectionProperties> allDbConnectionProperties) {
        Optional<DBConnectionProperties> defaultDataSource = DBConnectionProperties.finnDefault(allDbConnectionProperties);
        defaultDataSource.ifPresent(DatabaseStøtte::settOppJndiDataSource);
    }

    private static void kjørerMigreringFor(DBConnectionProperties connectionProperties) {
        DataSource dataSource = ConnectionHandler.opprettFra(connectionProperties);
        settOppDBSkjema(dataSource, connectionProperties);
    }

    private static void settOppJndiDataSource(DBConnectionProperties defaultConnectionProperties) {
        DataSource dataSource = ConnectionHandler.opprettFra(defaultConnectionProperties);
        try {
            new EnvEntry("jdbc/" + defaultConnectionProperties.getDatasource(), dataSource); // NOSONAR
        } catch (NamingException e) {
            throw new RuntimeException("Feil under registrering av JDNI-entry for default datasource", e); // NOSONAR
        }
    }

    private static void settOppDBSkjema(DataSource dataSource, DBConnectionProperties dbProperties) {
        if (dbProperties.isMigrateClean()) {
            migrer(dataSource, dbProperties, true);
        } else {
            migrer(dataSource, dbProperties, false);
        }
    }

    private static void migrer(DataSource dataSource,
                               DBConnectionProperties connectionProperties,
                               boolean cleanup) {
        String scriptLocation;
        if (connectionProperties.getMigrationScriptsClasspathRoot() != null) {
            scriptLocation = "classpath:/" + connectionProperties.getMigrationScriptsClasspathRoot() + "/"
                + connectionProperties.getSchema();
        } else {
            scriptLocation = getMigrationScriptLocation(connectionProperties);
        }

        boolean migreringOk = FlywayKonfig.lagKonfig(dataSource)
            .medSqlLokasjon(scriptLocation)
            .medCleanup(cleanup, connectionProperties.getUser())
            .medMetadataTabell(connectionProperties.getVersjonstabell())
            .migrerDb();

        if (!migreringOk) {
            LOGGER.warn(
                "\n\nKunne ikke starte inkrementell oppdatering av databasen. Det finnes trolig endringer i allerede kjørte script.\nKjører full migrering...");

            migreringOk = FlywayKonfig.lagKonfig(dataSource)
                .medCleanup(true, connectionProperties.getUser())
                .medSqlLokasjon(scriptLocation)
                .medMetadataTabell(connectionProperties.getVersjonstabell())
                .migrerDb();
            if (!migreringOk) {
                throw new IllegalStateException("\n\nFeil i script. Avslutter...");
            }
        }
    }

    private static String getMigrationScriptLocation(DBConnectionProperties connectionProperties) {
        String relativePath = connectionProperties.getMigrationScriptsFilesystemRoot() + connectionProperties.getDatasource();
        File baseDir = new File(".").getAbsoluteFile();
        File location = new File(baseDir, relativePath);
        while (!location.exists()) {
            baseDir = baseDir.getParentFile();
            if (baseDir == null || !baseDir.isDirectory()) {
                throw new IllegalArgumentException("Klarte ikke finne : " + baseDir);
            }
            location = new File(baseDir, relativePath);
        }

        return "filesystem:" + location.getPath();
    }


}
