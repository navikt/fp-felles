package no.nav.vedtak.felles.testutilities.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.feil.FeilFactory;

/**
 * Setter opp Flyway og databasemigreringer for enhetstester og testdata.
 * @deprecated Sett heller opp i app som benytter direkte
 */
@Deprecated(forRemoval=true)
public class FlywayKonfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayKonfig.class);

    private DataSource dataSource;
    private boolean cleanup = false;
    private String sqlLokasjon = null;
    private EntityManager entityManager = null;
    private String tabellnavn = null;

    /**
     * Username i db. Brukes til å rense alle tilhørende objekter (KUN FOR TEST!!).
     */
    private String username;

    private FlywayKonfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static FlywayKonfig lagKonfig(DataSource dataSource) {
        return new FlywayKonfig(dataSource);
    }

    public FlywayKonfig medCleanup(boolean utførFullMigrering, String username) {
        this.cleanup = utførFullMigrering;
        this.username = username;
        return this;
    }

    public FlywayKonfig medSqlLokasjon(String sqlLokasjon) {
        this.sqlLokasjon = sqlLokasjon;
        return this;
    }

    public FlywayKonfig medMetadataTabell(String tabellnavn) {
        this.tabellnavn = tabellnavn;
        return this;
    }


    public boolean migrerDb() {
        Flyway flyway = new Flyway();
        flyway.setBaselineOnMigrate(true);
        flyway.setDataSource(dataSource);

        if (tabellnavn != null) {
            flyway.setTable(tabellnavn);
        }

        if (sqlLokasjon != null) {
            flyway.setLocations(sqlLokasjon);
        } else {
            /**
             * Default leter flyway etter classpath:db/migration.
             * Her vet vi at vi ikke skal lete i classpath
             */
            flyway.setLocations("denne/stien/finnes/ikke");
        }

        if (cleanup) {
            if (skalBrukeCustomClean(dataSource)) {
                clean(dataSource, username);
            } else {
                flyway.clean();
            }
        }

        flyway.configure(lesFlywayPlaceholders());

        try {
            flyway.migrate();
            return true;
        } catch (FlywayException flywayException) {
            FeilFactory.create(DbMigreringFeil.class).flywayMigreringFeilet(flywayException).log(LOGGER);
            return false;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    private boolean skalBrukeCustomClean(DataSource dataSource) {
        try (var conn = dataSource.getConnection()) {
            String databaseProductName = conn.getMetaData().getDatabaseProductName();
            return "PostgreSQL".equalsIgnoreCase(databaseProductName);
        } catch (SQLException e) {
            throw FeilFactory.create(DbMigreringFeil.class).kanIkkeDetektereDatbaseType(e).toException();
        }
    }

    private Properties lesFlywayPlaceholders() {
        Properties placeholders = new Properties();
        for (String prop : System.getProperties().stringPropertyNames()) {
            if (prop.startsWith("flyway.placeholders.")) {
                placeholders.setProperty(prop, System.getProperty(prop));
            }
        }
        return placeholders;
    }

    /**
     * For å postgres - unngår issue med manglende support i flyway.
     */
    private void clean(DataSource dataSource, String username) {
        try (Connection c = dataSource.getConnection();
             Statement stmt = c.createStatement()) {
            stmt.execute("drop owned by \"" + username.replaceAll("[^a-zA-Z0-9_-]", "_") + "\""); // NOSONAR ok her, test konfig
        } catch (SQLException e) {
            throw new IllegalStateException("Kunne ikke kjøre clean på db", e);
        }
    }


}
