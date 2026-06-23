package no.nav.vedtak.felles.testutilities.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/** Felles strukturtester for Oracle. */
public abstract class AbstractOracleDbStrukturTest extends AbstractDbStrukturTest {

    /** Schema-eier brukt i Oracle ALL_*-views. */
    protected abstract String getOwner();

    /**
     * Tabellnavn-mønstre (LIKE-syntax) som ekskluderes fra alle sjekker.
     * Eksempel: {@code List.of("%_MOCK", "HTE_%")}
     */
    protected List<String> ekskluderteTabellmønstre() {
        return List.of();
    }

    /**
     * Kolonnenavn (eksakt, upper-case) som ekskluderes fra kolonnesjekken.
     * Eksempel: {@code List.of("LANDKODE")}
     */
    protected List<String> ekskluderteKolonner() {
        return List.of();
    }

    /** Bygger AND upper(kolonneRef) NOT LIKE 'mønster' for hvert ekskludert tabellmønster. */
    private String tabellFilter(String kolonneRef) {
        return ekskluderteTabellmønstre().stream()
            .map(m -> "  AND upper(" + kolonneRef + ") NOT LIKE '" + m.toUpperCase() + "'")
            .collect(Collectors.joining("\n"));
    }

    /** Bygger AND upper(kolonneRef) NOT IN ('KOL1','KOL2',...) hvis listen ikke er tom. */
    private String kolonneFilter(String kolonneRef) {
        if (ekskluderteKolonner().isEmpty()) return "";
        var verdier = ekskluderteKolonner().stream()
            .map(k -> "'" + k.toUpperCase() + "'")
            .collect(Collectors.joining(","));
        return "  AND upper(" + kolonneRef + ") NOT IN (" + verdier + ")";
    }

    @Test
    void sjekk_at_alle_tabeller_er_dokumentert() {
        var sql = """
            SELECT table_name FROM ALL_TAB_COMMENTS
            WHERE (comments IS NULL OR comments IN ('','MISSING COLUMN COMMENT'))
              AND owner = sys_context('userenv','current_schema')
              AND upper(table_name) NOT LIKE '%SCHEMA_%'
            """ + tabellFilter("table_name");
        assertThat(runSingleColumnQuery(sql)).isEmpty();
    }

    @Test
    void sjekk_at_alle_relevante_kolonner_er_dokumentert() {
        var sql = """
            SELECT t.owner||'.'||t.table_name||'.'||t.column_name
            FROM ALL_COL_COMMENTS t
            WHERE (t.comments IS NULL OR t.comments = '')
              AND t.owner = sys_context('userenv','current_schema')
              AND upper(t.table_name) NOT LIKE '%SCHEMA_%'
              AND NOT EXISTS (
                SELECT 1 FROM ALL_CONSTRAINTS a, ALL_CONS_COLUMNS b
                WHERE a.table_name = b.table_name
                  AND b.table_name = t.table_name
                  AND a.constraint_name = b.constraint_name
                  AND b.column_name = t.column_name
                  AND constraint_type IN ('P','R')
                  AND a.owner = t.owner
                  AND b.owner = a.owner)
              AND upper(t.column_name) NOT IN
                ('OPPRETTET_TID','ENDRET_TID','OPPRETTET_AV','ENDRET_AV',
                 'VERSJON','BESKRIVELSE','NAVN','FOM','TOM','AKTIV')
            """ + kolonneFilter("t.column_name") + "\n" + tabellFilter("t.table_name") + """

            ORDER BY t.table_name, t.column_name
            """;
        var avvik = runSingleColumnQuery(sql).stream().map(r -> "\n" + r).toList();
        assertThat(avvik)
            .withFailMessage("Mangler dokumentasjon for %s kolonner. %s%n%nGå over SQL-skriptene og dokumenter tabellene.",
                avvik.size(), avvik)
            .isEmpty();
    }

    @Test
    void sjekk_at_alle_FK_kolonner_har_fornuftig_indekser() {
        var sql = """
            SELECT UC.TABLE_NAME, UC.CONSTRAINT_NAME,
                   LISTAGG(DCC.COLUMN_NAME, ',') WITHIN GROUP (ORDER BY DCC.POSITION)
            FROM ALL_CONSTRAINTS UC
            INNER JOIN ALL_CONS_COLUMNS DCC
              ON DCC.CONSTRAINT_NAME = UC.CONSTRAINT_NAME AND DCC.OWNER = UC.OWNER
            WHERE UC.CONSTRAINT_TYPE = 'R'
              AND upper(UC.OWNER) = upper(:owner)
            """ + tabellFilter("UC.TABLE_NAME") + """

              AND EXISTS (
                SELECT UCC.POSITION, UCC.COLUMN_NAME
                FROM ALL_CONS_COLUMNS UCC
                WHERE UCC.CONSTRAINT_NAME = UC.CONSTRAINT_NAME AND UC.OWNER = UCC.OWNER
                MINUS
                SELECT UIC.COLUMN_POSITION, UIC.COLUMN_NAME
                FROM ALL_IND_COLUMNS UIC
                WHERE UIC.TABLE_NAME = UC.TABLE_NAME AND UIC.TABLE_OWNER = UC.OWNER
              )
            GROUP BY UC.TABLE_NAME, UC.CONSTRAINT_NAME
            ORDER BY UC.TABLE_NAME
            """;
        var q = getEntityManager().createNativeQuery(sql, Object[].class);
        q.setParameter("owner", getOwner());
        List<Object[]> rows = q.getResultList();
        assertThat(rows)
            .withFailMessage("Kolonner som inngår i Foreign Keys skal ha indekser. Mangler indekser for %s foreign keys%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }

    @Test
    void skal_ha_primary_key_i_hver_tabell_som_begynner_med_PK() {
        var sql = """
            SELECT table_name FROM all_tables at
            WHERE table_name NOT IN (
              SELECT ac.table_name FROM all_constraints ac
              WHERE ac.constraint_type = 'P'
                AND at.owner = ac.owner
                AND ac.constraint_name LIKE 'PK_%')
              AND upper(at.owner) = upper(:owner)
              AND upper(at.table_name) NOT LIKE '%SCHEMA_%'
            """ + tabellFilter("at.table_name");
        var q = getEntityManager().createNativeQuery(sql, String.class);
        q.setParameter("owner", getOwner());
        List<String> avvik = q.getResultList();
        assertThat(avvik)
            .withFailMessage("Feil eller manglende primary key (skal hete 'PK_<tabell navn>'). Antall feil = %s%n%nTabell:%n%s",
                avvik.size(), String.join("\n", avvik))
            .isEmpty();
    }

    @Test
    void skal_ha_alle_foreign_keys_begynne_med_FK() {
        var sql = """
            SELECT ac.table_name, ac.constraint_name FROM all_constraints ac
            WHERE ac.constraint_type = 'R'
              AND upper(ac.owner) = upper(:owner)
              AND constraint_name NOT LIKE 'FK_%'
            """ + tabellFilter("ac.table_name");
        var q = getEntityManager().createNativeQuery(sql, Object[].class);
        q.setParameter("owner", getOwner());
        List<Object[]> rows = q.getResultList();
        assertThat(rows)
            .withFailMessage("Feil eller manglende foreign key (skal hete 'FK_<tabell navn>_<løpenummer>'). Antall feil = %s%n%nTabell, Foreign Key%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }

    @Test
    void skal_ha_korrekt_index_navn() {
        var sql = """
            SELECT table_name, index_name, column_name
            FROM all_ind_columns
            WHERE table_owner = upper(:owner)
              AND index_name NOT LIKE 'PK_%'
              AND index_name NOT LIKE 'IDX_%'
              AND index_name NOT LIKE 'UIDX_%'
              AND upper(table_name) NOT LIKE '%SCHEMA_%'
              AND upper(table_name) NOT LIKE 'BIN$%'
            """ + tabellFilter("table_name");
        var q = getEntityManager().createNativeQuery(sql, Object[].class);
        q.setParameter("owner", getOwner());
        List<Object[]> rows = q.getResultList();
        assertThat(rows)
            .withFailMessage("Feil navngiving av index (PK_, UIDX_, IDX_). Antall feil = %s%n%nTabell, Index, Kolonne%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }

    @Test
    void skal_ha_samme_data_type_for_begge_sider_av_en_FK() {
        var sql = """
            SELECT TO_CHAR(T.TABLE_NAME), TO_CHAR(TCC.COLUMN_NAME),
                   TO_CHAR(ATT.DATA_TYPE), TO_CHAR(ATT.CHAR_LENGTH), TO_CHAR(ATT.CHAR_USED),
                   TO_CHAR(RCC.COLUMN_NAME),
                   TO_CHAR(ATR.DATA_TYPE), TO_CHAR(ATR.CHAR_LENGTH), TO_CHAR(ATR.CHAR_USED)
            FROM ALL_CONSTRAINTS T
            INNER JOIN ALL_CONSTRAINTS R
              ON R.OWNER = T.OWNER AND R.CONSTRAINT_NAME = T.R_CONSTRAINT_NAME
            INNER JOIN ALL_CONS_COLUMNS TCC
              ON TCC.TABLE_NAME = T.TABLE_NAME AND TCC.OWNER = T.OWNER
             AND TCC.CONSTRAINT_NAME = T.CONSTRAINT_NAME
            INNER JOIN ALL_CONS_COLUMNS RCC
              ON RCC.TABLE_NAME = R.TABLE_NAME AND RCC.OWNER = R.OWNER
             AND RCC.CONSTRAINT_NAME = R.CONSTRAINT_NAME
            INNER JOIN ALL_TAB_COLS ATT
              ON ATT.COLUMN_NAME = TCC.COLUMN_NAME AND ATT.OWNER = TCC.OWNER
             AND ATT.TABLE_NAME = TCC.TABLE_NAME
            INNER JOIN ALL_TAB_COLS ATR
              ON ATR.COLUMN_NAME = RCC.COLUMN_NAME AND ATR.OWNER = RCC.OWNER
             AND ATR.TABLE_NAME = RCC.TABLE_NAME
            WHERE T.OWNER = upper(:owner)
              AND T.CONSTRAINT_TYPE = 'R'
              AND TCC.POSITION = RCC.POSITION
              AND TCC.POSITION IS NOT NULL AND RCC.POSITION IS NOT NULL
              AND ((ATT.DATA_TYPE != ATR.DATA_TYPE)
                OR (ATT.CHAR_LENGTH != ATR.CHAR_LENGTH OR ATT.CHAR_USED != ATR.CHAR_USED)
                OR (ATT.DATA_TYPE NOT LIKE '%CHAR%' AND ATT.DATA_LENGTH != ATR.DATA_LENGTH))
            """ + tabellFilter("T.TABLE_NAME") + """

            ORDER BY T.TABLE_NAME, TCC.COLUMN_NAME
            """;
        var q = getEntityManager().createNativeQuery(sql, Object[].class);
        q.setParameter("owner", getOwner());
        List<Object[]> rows = q.getResultList();
        assertThat(rows)
            .withFailMessage("Forskjellig datatype på FK-sider (husk VARCHAR2(100 CHAR)). Antall feil = %s%n%nTABELL, KOL_A, KOL_A_DATA_TYPE, KOL_A_CHAR_LENGTH, KOL_A_CHAR_USED, KOL_B, KOL_B_DATA_TYPE, KOL_B_CHAR_LENGTH, KOL_B_CHAR_USED%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }

    @Test
    void skal_deklarere_VARCHAR2_kolonner_som_CHAR_ikke_BYTE_semantikk() {
        var sql = """
            SELECT TO_CHAR(TABLE_NAME), TO_CHAR(COLUMN_NAME),
                   TO_CHAR(DATA_TYPE), TO_CHAR(CHAR_USED), TO_CHAR(CHAR_LENGTH)
            FROM ALL_TAB_COLS
            WHERE DATA_TYPE = 'VARCHAR2'
              AND CHAR_USED != 'C'
              AND upper(TABLE_NAME) NOT LIKE '%SCHEMA%'
              AND CHAR_LENGTH > 1
              AND OWNER = upper(:owner)
            """ + tabellFilter("TABLE_NAME") + """

            ORDER BY TABLE_NAME, COLUMN_NAME
            """;
        var q = getEntityManager().createNativeQuery(sql, Object[].class);
        q.setParameter("owner", getOwner());
        List<Object[]> rows = q.getResultList();
        assertThat(rows)
            .withFailMessage("Feil deklarasjon av VARCHAR2 (husk VARCHAR2(100 CHAR)). Antall feil = %s%n%nTABELL, KOLONNE, DATA_TYPE, CHAR_USED, CHAR_LENGTH%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }

    @Test
    void skal_ikke_bruke_FLOAT_eller_DOUBLE() {
        var sql = """
            SELECT TO_CHAR(TABLE_NAME), TO_CHAR(COLUMN_NAME), TO_CHAR(DATA_TYPE)
            FROM ALL_TAB_COLS
            WHERE OWNER = upper(:owner)
              AND DATA_TYPE IN ('FLOAT', 'DOUBLE')
            """ + tabellFilter("TABLE_NAME") + """

            ORDER BY TABLE_NAME, COLUMN_NAME
            """;
        var q = getEntityManager().createNativeQuery(sql, Object[].class);
        q.setParameter("owner", getOwner());
        List<Object[]> rows = q.getResultList();
        assertThat(rows)
            .withFailMessage("Feil bruk av datatype, skal ikke ha FLOAT eller DOUBLE (bruk NUMBER for alle desimaltall, spesielt der penger representeres). Antall feil = %s%n%nTabell, Kolonne, Datatype%n%s",
                rows.size(), joinRows(rows))
            .isEmpty();
    }
}
