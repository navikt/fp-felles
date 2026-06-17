package no.nav.vedtak.felles.jpa.jdbc;

import java.util.Optional;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.micrometer.core.instrument.Metrics;

public final class DatasourceUtil {

    private DatasourceUtil() {
    }

    public static HikariDataSource postgresDataSource(String jdbcUrl, String username, String password, int maxPoolSize) {
        var hikariConfig = postgresDataSourceConfig(jdbcUrl, username, password, maxPoolSize);
        return new HikariDataSource(hikariConfig);
    }

    public static HikariConfig postgresDataSourceConfig(String jdbcUrl, String username, String password, int maxPoolSize) {
        var config = createDataSourceConfig(jdbcUrl, username, password, maxPoolSize);
        config.setMetricRegistry(Metrics.globalRegistry);

        // optimaliserer inserts for postgres
        var dsProperties = new Properties();
        dsProperties.setProperty("reWriteBatchedInserts", "true");
        dsProperties.setProperty("logServerErrorDetail", "false"); // skrur av batch exceptions som lekker statements i åpen logg
        config.setDataSourceProperties(dsProperties);

        return config;
    }

    public static HikariDataSource oracleDataSource(String jdbcUrl, String username, String password, int maxPoolSize) {
        var config = createDataSourceConfig(jdbcUrl, username, password, maxPoolSize);
        config.setMetricRegistry(Metrics.globalRegistry);

        // Kan vurdere å ta inn disse to som Properties - samme metode som for postgres
        // ("oracle.jdbc.implicitStatementCacheSize", "50"); // Gjerne litt over antall entiteter
        // ("oracle.jdbc.defaultConnectionValidation", "LOCAL"); // LOCAL, SOCKET, NETWORK (default)

        return new HikariDataSource(config);
    }

    public static HikariConfig createDataSourceConfig(String jdbcUrl, String username, String password, int maxPoolSize) {
        var config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        Optional.ofNullable(username).ifPresent(config::setUsername);
        Optional.ofNullable(password).ifPresent(config::setPassword);
        config.setConnectionTimeout(2500);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(maxPoolSize / 2);
        // Styres av EntityManager
        config.setAutoCommit(false);
        return config;
    }

}
