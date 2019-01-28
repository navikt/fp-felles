package no.nav.vedtak.felles.db.doc;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.sql.DataSource;
import javax.tools.Diagnostic.Kind;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import no.nav.vedtak.felles.db.doc.model.Column;
import no.nav.vedtak.felles.db.doc.model.ForeignKey;
import no.nav.vedtak.felles.db.doc.model.JdbcModel;
import no.nav.vedtak.felles.db.doc.model.Kodeverk;
import no.nav.vedtak.felles.db.doc.model.Table;

/** Migrer mot en H2 db (hvis tilgjengelig på classpath), og dokumenterer struktur. */
public class JdbcDoclet implements Doclet {

    private static final String INMEMORY_DB_JDBC_URL = "jdbc:h2:./TEST;MODE=PostgreSQL";
    private static final String INMEMORY_DB_USER = "sa";

    private static final String dsNames = System.getProperty("doc.plugin.jdbc.dslist", "defaultDS");
    
    @SuppressWarnings("unused")
    private Locale locale;
    private Reporter reporter;
    private String schemaOverride;

    public JdbcDoclet() {
    }
    
    public JdbcDoclet(String schemaNavn) {
        this.schemaOverride = schemaNavn;
    }
    
    @Override
    public void init(Locale locale, Reporter reporter) {
        this.locale = locale;
        this.reporter = reporter;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Collections.emptySet();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_10;
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        System.out.println("Kjører Javadoc Doclet - " + getClass().getSimpleName());
        String[] dsList = dsNames.split(",");
        try {
            for (String dsName : dsList) {
                JdbcModel jdbcModel = new JdbcModel();
                DataSource ds = initDataSource(dsName);
                readJdbcModel(ds, jdbcModel, dsName);
                Kodeverk.readReferenceData(jdbcModel, ds);
                writeJdbcModel(jdbcModel, dsName);
            }
            return true;
        } catch (SQLException | Error | RuntimeException e) {
            reporter.print(Kind.ERROR, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    DataSource initDataSource(String dsName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getJdbcUrl());
        config.setUsername(getJdbcUserName(dsName));
        config.setPassword(getJdbcUsernamePassword(config.getUsername(), dsName));
        config.setAutoCommit(true);
        config.addDataSourceProperty("remarksReporting", true);
        config.setMaximumPoolSize(2);
        String dbDriver = getDatabaseDriver();
        if (dbDriver != null)
            config.setDriverClassName(dbDriver);
        HikariDataSource dataSource = new HikariDataSource(config);
        initMigrations(dataSource, dsName, config.getUsername());
        return dataSource;

    }

    private String getJdbcUserName(String dsName) {
        return getEnvOrDefaultValue("doc.plugin.jdbc.username." + dsName, INMEMORY_DB_USER);
    }

    private String getDSmappe(String dsName) {
        return getEnvOrDefaultValue("doc.plugin.jdbc.db.migration." + dsName, "classpath:db/migration");
    }

    void initMigrations(HikariDataSource dataSource, String dsName, String username) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations(getDSmappe(dsName));
        try {
            flyway.migrate();
        } catch (FlywayException e) {
            clean(dataSource, username);
            flyway.migrate();
        }
    }
    
    private void clean(DataSource dataSource, String username) {
        try (Connection c = dataSource.getConnection();
                Statement stmt = c.createStatement()) {
            stmt.execute("drop owned by " + username.replaceAll("[^a-zA-Z0-9_-]", "_"));
        } catch (SQLException e) {
            throw new IllegalStateException("Kunne ikke kjøre clean på db", e);
        }
    }

    private File getOutputLocation() {
        File dir = new File(System.getProperty("destDir", "target/jdbc"));
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("Could not create output directory:" + dir);
            }
        }
        return dir;
    }

    private String getJdbcUsernamePassword(String username, String dsName) {
        return getEnvOrDefaultValue("doc.plugin.jdbc.password." + dsName, username);
    }

    private String getDatabaseDriver() {
        return getEnvOrDefaultValue("doc.plugin.jdbc.driver", null);
    }

    private String getSchemaName(String dsName) {
        return this.schemaOverride!=null?this.schemaOverride : getEnvOrDefaultValue("doc.plugin.jdbc.schema." + dsName, getJdbcUserName(dsName).toUpperCase());
    }

    private String getJdbcUrl() {
        return getEnvOrDefaultValue("doc.plugin.jdbc.url", INMEMORY_DB_JDBC_URL);
    }

    private String getEnvOrDefaultValue(String key, String defaultValue) {
        return Optional.ofNullable(System.getenv(key)).orElse(System.getProperty(key, defaultValue));
    }

    private void writeJdbcModel(JdbcModel jdbcModel, String dsName) {
        writeToAsciidoc(jdbcModel, dsName);
    }

    private void writeToAsciidoc(JdbcModel jdbcModel, String dsName) {
        AsciidocMapper mapper = new AsciidocMapper();
        File outputFile = new File(getOutputLocation(), dsName);
        mapper.writeTo(outputFile.toPath(), jdbcModel);
    }

    private void readJdbcModel(DataSource ds, JdbcModel jdbcModel, String dsName) throws SQLException {
        try (Connection c = ds.getConnection()) {
            DatabaseMetaData metaData = c.getMetaData();
            String catalog = c.getCatalog();
            try (ResultSet tables = metaData.getTables(catalog, getSchemaName(dsName), "%", new String[] { "TABLE", "VIEW" });) {

                while (tables.next()) {
                    String schema = tables.getString("TABLE_SCHEM");
                    String tableName = tables.getString("TABLE_NAME");
                    String type = tables.getString("TABLE_TYPE");
                    String ddl = null; // støttes ikke av Oracle
                    String remarks = tables.getString("REMARKS");
                    List<String> primaryKeyColumns = getPrimaryKeyColumns(metaData, catalog, schema, tableName);

                    Table table = new Table(tableName, type, remarks).withDdl(ddl);
                    jdbcModel.addTable(table);

                    readColumns(metaData, catalog, tableName, table, primaryKeyColumns, dsName);

                    if (table.isTable()) {
                        readForeignKeys(c, metaData, tableName, jdbcModel, table, dsName);
                    }
                }
            }
        }
    }

    private List<String> getPrimaryKeyColumns(DatabaseMetaData metaData, String catalog, String schema, String tableName)
            throws SQLException {
        List<String> pks = new ArrayList<>();
        try (ResultSet primaryKeys = metaData.getPrimaryKeys(catalog, schema, tableName)) {
            while (primaryKeys.next()) {
                pks.add(primaryKeys.getString("COLUMN_NAME"));
            }
        }
        return pks;
    }

    @SuppressWarnings("unused")
    private void readForeignKeys(Connection c, DatabaseMetaData metaData, String name, JdbcModel jdbcModel, Table table, String dsName)
            throws SQLException {
        try (ResultSet foreignKeys = metaData.getImportedKeys(c.getCatalog(), getSchemaName(dsName), name);) {
            while (foreignKeys.next()) {
                String fkTableName = foreignKeys.getString("FKTABLE_NAME");
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");

                table.addForeignKey(new ForeignKey(fkTableName, fkColumnName, pkTableName, pkColumnName));
            }
        }
    }

    private void readColumns(DatabaseMetaData metaData, String catalog, String name, Table table, List<String> primaryKeyColumns, String dsName)
            throws SQLException {
        try (ResultSet columns = metaData.getColumns(catalog, getSchemaName(dsName), name, "%");) {
            while (columns.next()) {
                String colName = columns.getString("COLUMN_NAME");
                String colType = columns.getString("TYPE_NAME");
                int colSize = columns.getInt("COLUMN_SIZE");
                String remarks = columns.getString("REMARKS");
                String defaultValue = columns.getString("COLUMN_DEF");

                // YES = ISO def av nullable
                boolean isNullable = "YES".equals(columns.getString("IS_NULLABLE"));

                Column column = new Column(colName, colType, colSize, defaultValue, isNullable, remarks,
                    primaryKeyColumns.contains(colName));
                table.addColumn(column);
            }
        }
    }

}
