package no.nav.vedtak.felles.testutilities.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Felles strukturtester for PostgreSQL. */
public abstract class AbstractPostgresDbStrukturTest extends AbstractDbStrukturTest {

    @Test
    @DisplayName("Test at alle tabeller er kommentert.")
    void sjekk_at_alle_tabeller_er_dokumentert() {
        var sql = """
            SELECT t.table_name
            FROM information_schema.tables t
            JOIN pg_class c ON t.table_name = c.relname
            WHERE t.table_schema = current_schema
              AND t.table_name NOT LIKE 'flyway_%'
              AND t.table_name NOT LIKE 'prosess_task_partition_%'
              AND obj_description(c.oid) IS NULL
            """;
        assertThat(runSingleColumnQuery(sql)).isEmpty();
    }

    @Test
    @DisplayName("Test at alle kolonner er kommentert. Bortsett fra PK og FK kolonner.")
    void sjekk_at_alle_relevante_kolonner_er_dokumentert() {
        var sql = """
            SELECT c.table_name||'.'||c.column_name
            FROM pg_catalog.pg_statio_all_tables AS st
              RIGHT JOIN pg_catalog.pg_description pgd ON pgd.objoid = st.relid
              RIGHT JOIN information_schema.columns c
                ON pgd.objsubid = c.ordinal_position
               AND c.table_schema = st.schemaname
               AND c.table_name = st.relname
            WHERE c.table_schema = current_schema
              AND upper(c.column_name) NOT IN
                ('OPPRETTET_TID','ENDRET_TID','OPPRETTET_AV','ENDRET_AV',
                 'VERSJON','BESKRIVELSE','NAVN','FOM','TOM','AKTIV')
              AND c.table_name NOT LIKE 'flyway_%'
              AND c.table_name NOT LIKE 'prosess_task_partition_%'
              AND c.table_name NOT LIKE 'prosess_task'
              AND pgd.description IS NULL
              AND NOT EXISTS (
                SELECT 1
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                  ON tc.constraint_name = kcu.constraint_name
                 AND tc.table_schema = kcu.table_schema
                WHERE constraint_type IN ('FOREIGN KEY','PRIMARY KEY')
                  AND tc.table_schema = current_schema
                  AND tc.table_name = c.table_name
                  AND kcu.column_name = c.column_name
              )
            ORDER BY c.table_name, c.column_name
            """;
        var avvik = runSingleColumnQuery(sql).stream().map(r -> "\n" + r).toList();
        assertThat(avvik)
            .withFailMessage("Mangler dokumentasjon for %s kolonner. %s%n%nGå over SQL-skriptene og dokumenter tabellene.",
                avvik.size(), avvik)
            .isEmpty();
    }

    @Test
    @DisplayName("Test at alle primary keys har navn med pk_ prefix.")
    void skal_ha_primary_key_i_hver_tabell_som_begynner_med_PK() {
        var sql = """
            SELECT t.table_name
            FROM information_schema.tables t
            WHERE t.table_schema = current_schema
              AND t.table_name NOT LIKE 'flyway_%'
              AND t.table_name NOT LIKE 'prosess_task_partition_%'
              AND t.table_name NOT IN (
                SELECT c.table_name FROM information_schema.table_constraints c
                WHERE c.constraint_type = 'PRIMARY KEY' AND c.constraint_name LIKE 'pk_%'
              )
            """;
        var avvik = runSingleColumnQuery(sql);
        assertThat(avvik)
            .withFailMessage("Feil eller manglende primary key (skal hete 'pk_<tabell navn>'). Antall feil = %s%n%nTabell:%n%s",
                avvik.size(), String.join("\n", avvik))
            .isEmpty();
    }

    @Test
    @DisplayName("Test at alle foreign keys har navn med fk_ prefix.")
    void skal_ha_alle_foreign_keys_begynne_med_FK() {
        var sql = """
            SELECT table_name, constraint_name
            FROM information_schema.table_constraints
            WHERE constraint_type = 'FOREIGN KEY'
              AND table_schema = current_schema
              AND constraint_name NOT LIKE 'fk_%'
            """;
        var rows = runMultiColumnQuery(sql);
        assertThat(rows)
            .withFailMessage("Feil eller manglende foreign key (skal hete 'fk_<tabell navn>_<løpenummer>'). Antall feil = %s%n%nTabell, Foreign Key%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }

    @Test
    @DisplayName("Test at indekser har riktige prefix: pk_, uidx_, idx_, chk_")
    void skal_ha_korrekt_index_navn() {
        var sql = """
            SELECT t.relname, i.relname, a.attname
            FROM pg_class t, pg_class i, pg_index ix, pg_attribute a,
                 information_schema.tables it
            WHERE t.oid = ix.indrelid AND i.oid = ix.indexrelid
              AND a.attrelid = t.oid AND a.attnum = ANY(ix.indkey)
              AND t.relkind = 'r' AND t.relname = it.table_name
              AND it.table_schema = current_schema
              AND it.table_name NOT LIKE 'flyway_%'
              AND it.table_name NOT LIKE 'prosess_task_partition_%'
              AND i.relname NOT LIKE 'pk_%'
              AND i.relname NOT LIKE 'idx_%'
              AND i.relname NOT LIKE 'uidx_%'
              AND i.relname NOT LIKE 'chk_%'
            ORDER BY t.relname, i.relname
            """;
        var rows = runMultiColumnQuery(sql);
        assertThat(rows)
            .withFailMessage("Feil navngiving av index (pk_, uidx_, idx_, chk_). Antall feil = %s%n%nTabell, Index, Kolonne%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }

    @Test
    @DisplayName("Test at alle FK har like datatyper på begge sider.")
    void skal_ha_samme_data_type_for_begge_sider_av_en_FK() {
        var sql = """
            SELECT t1.relname, cs1.column_name, cs1.data_type,
                   cs1.character_maximum_length::text, cs1.character_octet_length::text,
                   cs2.column_name, cs2.data_type,
                   cs2.character_maximum_length::text, cs2.character_octet_length::text
            FROM pg_class t1, pg_class t2,
                 information_schema.columns cs1, information_schema.columns cs2,
                 LATERAL (
                   SELECT c.conrelid, unnest(c.conkey) AS conkeypos,
                          c.confrelid, unnest(c.confkey) AS confkeypos
                   FROM pg_constraint c WHERE c.contype = 'f'
                 ) AS fk
            WHERE fk.conrelid = t1.oid AND fk.confrelid = t2.oid
              AND t1.relname = cs1.table_name AND t2.relname = cs2.table_name
              AND fk.conkeypos = cs1.ordinal_position
              AND fk.confkeypos = cs2.ordinal_position
              AND cs1.table_schema = current_schema AND cs2.table_schema = current_schema
              AND cs1.table_name NOT LIKE 'prosess_task_partition_%'
              AND cs2.table_name NOT LIKE 'prosess_task_partition_%'
              AND (cs1.data_type != cs2.data_type
                OR cs1.character_maximum_length != cs2.character_maximum_length
                OR cs1.character_octet_length != cs2.character_octet_length
                OR cs1.numeric_precision != cs2.numeric_precision
                OR cs1.numeric_precision_radix != cs2.numeric_precision_radix
                OR cs1.numeric_scale != cs2.numeric_scale)
            """;
        var rows = runMultiColumnQuery(sql);
        assertThat(rows)
            .withFailMessage("Forskjellig datatype for kolonne på hver side av en FK. Antall feil = %s%n%nTABELL, KOL_A, KOL_A_DATA_TYPE, KOL_A_CHAR_LENGTH, KOL_A_CHAR_USED, KOL_B, KOL_B_DATA_TYPE, KOL_B_CHAR_LENGTH, KOL_B_CHAR_USED%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }

    @Test
    @DisplayName("Test at real, float, double precision datatyper ikke brukes.")
    void skal_ikke_bruke_FLOAT_REAL_eller_DOUBLEPRECISION() {
        var sql = """
            SELECT table_name, column_name, data_type
            FROM information_schema.columns
            WHERE table_schema = current_schema
              AND data_type IN ('real','double precision','float')
            """;
        var rows = runMultiColumnQuery(sql);
        assertThat(rows)
            .withFailMessage("Feil bruk av datatype REAL/FLOAT/DOUBLE PRECISION (bruk NUMERIC). Antall feil = %s%n%nTabell, Kolonne, Datatype%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }

    @Test
    @DisplayName("Test at alle FK-kolonner har en indeks.")
    void sjekk_at_alle_FK_kolonner_har_fornuftig_indekser() {
        var sql = """
            SELECT uc.table_name, uc.constraint_name,
                   STRING_AGG(dcc.column_name, ',' ORDER BY dcc.ordinal_position)
            FROM information_schema.table_constraints uc
            JOIN information_schema.key_column_usage dcc
              ON dcc.constraint_name = uc.constraint_name
             AND dcc.table_schema = uc.table_schema
            WHERE uc.constraint_type = 'FOREIGN KEY'
              AND uc.table_schema = current_schema
              AND EXISTS (
                SELECT ucc.column_name
                FROM information_schema.key_column_usage ucc
                WHERE ucc.constraint_name = uc.constraint_name
                  AND ucc.table_schema = uc.table_schema
                EXCEPT
                SELECT a.attname
                FROM pg_catalog.pg_index ix
                JOIN pg_catalog.pg_class t ON t.oid = ix.indrelid
                JOIN pg_catalog.pg_class i ON i.oid = ix.indexrelid
                JOIN pg_catalog.pg_attribute a ON a.attnum = ANY(ix.indkey) AND a.attrelid = t.oid
                WHERE t.relkind = 'r' AND t.relname = uc.table_name
              )
            GROUP BY uc.table_name, uc.constraint_name
            ORDER BY uc.table_name
            """;
        var rows = runMultiColumnQuery(sql);
        assertThat(rows)
            .withFailMessage("Kolonner som inngår i Foreign Keys skal ha indekser. Mangler indekser for %s foreign keys%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }

    @Test
    @DisplayName("Test at alle unike indekser har uidx_ prefix.")
    void sjekk_at_alle_unike_indekser_starter_med_uidx_prefiks() {
        var sql = """
            SELECT tbl.relname, idx.relname
            FROM pg_index pgi
            JOIN pg_class idx ON idx.oid = pgi.indexrelid
            JOIN pg_namespace insp ON insp.oid = idx.relnamespace
            JOIN pg_class tbl ON tbl.oid = pgi.indrelid
            JOIN pg_namespace tnsp ON tnsp.oid = tbl.relnamespace
            WHERE pgi.indisunique
              AND tbl.relname NOT LIKE 'flyway_%'
              AND tbl.relname NOT LIKE 'prosess_task_partition_%'
              AND idx.relname NOT LIKE 'pk_%'
              AND idx.relname NOT LIKE 'uidx_%'
              AND tnsp.nspname = current_schema
            ORDER BY tbl.relname
            """;
        var rows = runMultiColumnQuery(sql);
        assertThat(rows)
            .withFailMessage("Unike indekser skal starte med uidx_. Antall feil = %s%n%nTabell, Index%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }
}
