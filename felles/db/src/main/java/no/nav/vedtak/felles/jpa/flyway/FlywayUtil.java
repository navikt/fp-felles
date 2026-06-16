package no.nav.vedtak.felles.jpa.flyway;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.vedtak.felles.jpa.jdbc.DatasourceUtil;

public final class FlywayUtil {

    private FlywayUtil() {
    }

    // Standard migrering. Unntak: On-prem postgres med callback og Oracle med "schema-version"
    public static void migrate(DataSource ds, String location) {
        flywayConfig(ds, location)
            .load()
            .migrate();
    }

    // Må brukes fram til tabell schema_version (+constraint + 2 index) er renamed til flyway_schema_history
    public static void migrateLegacyOracle(DataSource ds, String location) {
        flywayConfig(ds, location)
            .table("schema_version")
            .load()
            .migrate();
    }

    public static FluentConfiguration flywayConfig(DataSource dataSource, String location) {
        return Flyway.configure().dataSource(dataSource).locations(location).baselineOnMigrate(true);
    }

    // Dedicated DataSource for use in a try/resources around migration. Not used elsewhere.
    public static HikariDataSource createMigrationDataSource(String jdbcUrl, String username, String password) {
        var hikariConfig = createMigrationDataSourceConfig(jdbcUrl, username, password);
        return new HikariDataSource(hikariConfig);
    }

    public static HikariConfig createMigrationDataSourceConfig(String jdbcUrl, String username, String password) {
        return DatasourceUtil.createDataSourceConfig(jdbcUrl, username, password, 3);
    }

}
