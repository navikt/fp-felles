package no.nav.vedtak.felles.db.doc;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DocletTest {

    @Test
    @Disabled("unødvendig normalt, kan brukes til feilsøking")
    public void test_migrate_ddl() throws Exception {
        initDatabase();
    }

    @Test
    public void test_get_metadata_from_database() throws Exception {
        initDatabase();
        new JdbcDoclet("PUBLIC").run(null);
    }

    private void initDatabase() {
        new JdbcDoclet().initDataSource("");
    }

}
