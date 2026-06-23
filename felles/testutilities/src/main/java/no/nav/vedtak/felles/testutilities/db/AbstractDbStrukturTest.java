package no.nav.vedtak.felles.testutilities.db;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Felles base for DB-strukturtester. */
public abstract class AbstractDbStrukturTest extends EntityManagerAwareTest {

    protected List<String> runSingleColumnQuery(String sql) {
        return getEntityManager().createNativeQuery(sql, String.class).getResultStream().toList();
    }

    protected List<Object[]> runMultiColumnQuery(String sql) {
        return getEntityManager().createNativeQuery(sql, Object[].class).getResultList();
    }

    protected String joinRows(List<Object[]> rows) {
        return rows.stream()
            .map(row -> Arrays.stream(row)
                .map(col -> col instanceof Character c ? c.toString() : (String) col)
                .collect(Collectors.joining(", ")))
            .collect(Collectors.joining("\n"));
    }
}
