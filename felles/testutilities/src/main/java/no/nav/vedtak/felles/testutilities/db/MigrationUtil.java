package no.nav.vedtak.felles.testutilities.db;

import java.io.File;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import no.nav.vedtak.felles.jpa.flyway.FlywayUtil;
import no.nav.vedtak.felles.jpa.jdbc.DatasourceUtil;

public final class MigrationUtil {

    private MigrationUtil() {
    }

    public static void migrateLocalBuildTest(DataSource ds, String scriptPath) {
        var flyway = FlywayUtil.flywayConfig(ds, scriptPath)
            .cleanDisabled(false)
            .load();
        try {
            flyway.migrate();
        } catch (Exception _) {
            flyway.clean();
            flyway.migrate();
        }
    }

    public static HikariDataSource createLocalBuildTestDataSource(String jdbcUrl, String username, String password) {
        var config = DatasourceUtil.createDataSourceConfig(jdbcUrl, username, password, 8);
        return new HikariDataSource(config);
    }

    // Trengs ved multi-modul-prosjekt - traverserer opp til man finner stien
    public static String getScriptLocation(String relativePath) {
        var baseDir = new File(".").getAbsoluteFile();
        var location = new File(baseDir, relativePath);
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
