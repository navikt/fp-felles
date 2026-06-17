package no.nav.vedtak.felles.jpa.jdbc;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

public class DataSourceHolder {

    private static DataSource ds;

    private DataSourceHolder() {
    }

    // Call this explicitly in your main method before launching Jetty/Weld
    public static void initialize(DataSource localDataSource) {
        ds = localDataSource;
        if (ds instanceof HikariDataSource hds) {
            Runtime.getRuntime().addShutdownHook(new Thread(hds::close));
        }
    }

    public static DataSource getDataSource() {
        if (ds == null) throw new IllegalStateException("DataSource not initialized.");
        return ds;
    }

    public static boolean isInitialized() {
        return ds != null;
    }

}
