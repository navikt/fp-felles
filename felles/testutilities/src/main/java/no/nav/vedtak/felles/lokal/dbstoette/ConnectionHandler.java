package no.nav.vedtak.felles.lokal.dbstoette;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionHandler {

    private static Map<String, DataSource> cache = new ConcurrentHashMap<>();

    private ConnectionHandler() {
    }

    public static synchronized DataSource opprettFra(DBConnectionProperties dbProperties) {

        if (cache.containsKey(dbProperties.getDatasource())) {
            return cache.get(dbProperties.getDatasource());
        }

        DataSource ds = opprettDatasource(dbProperties);
        cache.put(dbProperties.getDatasource(), ds);

        return ds;
    }

    private static DataSource opprettDatasource(DBConnectionProperties dbProperties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbProperties.getUrl());
        config.setUsername(dbProperties.getUser());
        config.setPassword(dbProperties.getPassword());

        config.setConnectionTimeout(1000);
        config.setMinimumIdle(0);
        config.setMaximumPoolSize(4);

        Properties dsProperties = new Properties();
        config.setDataSourceProperties(dsProperties);

        HikariDataSource hikariDataSource = new HikariDataSource(config);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                hikariDataSource.close();
            }
        }));
        
        return hikariDataSource;
    }
}
