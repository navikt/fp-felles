package no.nav.vedtak.felles.db.doc.model;

import java.util.ArrayList;
import java.util.List;

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;

public class JdbcModel implements MarkupOutput {

    private List<Table> tables = new ArrayList<>();

    private String schemaName;

    public JdbcModel() {
    }

    public JdbcModel(String schemaName) {
        this.schemaName = schemaName;
    }

    public void addTable(Table table) {
        tables.add(table);
    }

    public List<Table> getTables() {
        return tables;
    }

    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public void apply(int sectionLevel, MarkupDocBuilder doc) {
        sectionLevel++;
        applyTables(sectionLevel, doc);
        applyViews(sectionLevel, doc);
    }

    private void applyTables(int sectionLevel, MarkupDocBuilder doc) {
        doc.sectionTitleLevel(sectionLevel, "Tabeller");
        for (Table t : Table.filterTables(tables, true)) {
            t.apply(sectionLevel, doc);
        }

        doc.sectionTitleLevel(sectionLevel, "Kodeverk");
        for (Table t : Table.filterTables(tables, true)) {
            if (t.isKodeverk() && !t.isKodeliste()) {
                new Kodeverk(t).apply(sectionLevel, doc);
            }
        }

        doc.sectionTitleLevel(sectionLevel, "Kodeliste");
        for (Table t : Table.filterTables(tables, true)) {
            if (t.isKodeliste() && !t.isKodeverk()) {
                new Kodeliste(t).apply(sectionLevel, doc);
            }
        }
    }

    private void applyViews(int sectionLevel, MarkupDocBuilder doc) {
        doc.sectionTitleLevel(sectionLevel, "Views");
        for (Table t : Table.filterTables(tables, false)) {
            t.apply(sectionLevel, doc);
        }
    }
}
