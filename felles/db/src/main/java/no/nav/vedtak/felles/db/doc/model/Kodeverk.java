package no.nav.vedtak.felles.db.doc.model;

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupTableColumn;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Kodeverk implements MarkupOutput {

    public static final String KODEVERK_PK_SPEC = System.getProperty("doc.plugin.jdbc.kodeverk.pk", "KODE");
    protected static final Set<String> KODEVERK_UNNTAK_KOLONNER = toList(
            System.getProperty("doc.plugin.jdbc.kodeverk.kolonne.unntak",
                    "ENDRET_AV, OPPRETTET_AV, ENDRET_TID, OPPRETTET_TID"));

    private Table table;
    private Set<String> unntakKolonner = KODEVERK_UNNTAK_KOLONNER;

    public Kodeverk(Table table) {
        this.table = table;
    }

    @Override
    public void apply(int sectionLevel, MarkupDocBuilder doc) {
        doc.sectionTitleLevel(sectionLevel + 1, table.getName().toUpperCase());
        if (!table.isErDelTabell()) table.writeTableComment(doc);

        List<MarkupTableColumn> columnSpecs = new ArrayList<>();

        for (Column c : table.getColumnsExcept(unntakKolonner)) {
            columnSpecs.add(new MarkupTableColumn(c.getName()).withHeaderColumn(c.isPrimaryKey()));
        }

        final List<java.util.List<String>> cells = new ArrayList<>();
        final AtomicInteger i = new AtomicInteger();
        table.getRows().forEach(c -> {
            if (i.incrementAndGet() >= 1) {
                // skipper første rad, inneholder kun headere
                cells.add(c);
            }
        });

        if (cells.isEmpty()) {
            cells.add(Collections.nCopies(columnSpecs.size(), ""));
        }
        doc.tableWithColumnSpecs(columnSpecs, cells);
    }

    public static Set<String> toList(String property) {
        Set<String> list = new HashSet<>();

        try (Scanner scanner = new Scanner(property).useDelimiter(",\\s*");) {
            while (scanner.hasNext()) {
                list.add(scanner.next());
            }
            return list;
        }
    }

    public static void readReferenceData(JdbcModel jdbcModel, DataSource ds) throws SQLException {
        try (Connection c = ds.getConnection()) {
            for (Table table : jdbcModel.getTables()) {
                if (table.isKodeverk() || table.isKodeliste()) {
                    List<Column> selectColumns = table.getColumnsExcept(KODEVERK_UNNTAK_KOLONNER);
                    readRows(c, table, selectColumns.stream().map(col -> col.getName()).collect(Collectors.toList()));
                }
            }
        }
    }

    static void readRows(Connection c, Table table, List<String> selectColumns) throws SQLException {
        int numRows = 0;
        try (PreparedStatement ps = c
                .prepareStatement("select " + String.join(", ", selectColumns) + " from " + table.getName());
             ResultSet rs = ps.executeQuery();
             ) {

            while (rs.next()) {
                numRows++;
                List<String> row = new ArrayList<>();
                for (int i = 0; i < selectColumns.size(); i++) {
                    String column = selectColumns.get(i);
                    String value = rs.getString(column);
                    row.add(value);
                }

                if (table.isKodeliste()) {
                    Optional<List<String>> filteredRow = filterKodelisteRow(selectColumns, row);

                    if (filteredRow.isPresent()) {
                        table.addRow(filteredRow.get(), false);
                    }
                } else {
                    table.addRow(row, false);
                    System.out.println(row);
                }

            }
        }
        if (numRows > 0) {
            table.addRow(selectColumns, true);
        }
    }

    private static Optional<List<String>> filterKodelisteRow(List<String> selectColumns, List<String> row) {
        int colIndex = selectColumns.indexOf("KODEVERK");
        if ("POSTSTED".equalsIgnoreCase(row.get(colIndex))) {
            return Optional.empty();
        }
        return Optional.of(row);
    }
}
