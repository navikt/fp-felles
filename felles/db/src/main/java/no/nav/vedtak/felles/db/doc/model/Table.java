package no.nav.vedtak.felles.db.doc.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.github.swagger2markup.markup.builder.MarkupBlockStyle;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupTableColumn;

public class Table implements MarkupOutput {

    private final String name;

    private final boolean erDelTabell;

    private String ddl;
    private final String type;
    private final String comment;
    private final List<Column> columns = new ArrayList<>();
    private final List<ForeignKey> importedKeys = new ArrayList<>();
    private final List<List<String>> data = new ArrayList<>();
    public Table(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
        this.erDelTabell = false;
    }

    public Table(String name, String type, String comment, boolean erDelTabell) {
        this.name = name;
        this.type = type;
        this.comment = comment;
        this.erDelTabell = erDelTabell;
    }

    public String getComment() {
        return comment;
    }

    public List<Column> getColumns() {
        Comparator<Column> comp = primaryKeyFirstComparator();
        return columns.stream().sorted(comp).collect(Collectors.toList());
    }

    public boolean isErDelTabell() {
        return erDelTabell;
    }

    public String getName() {
        return name;
    }

    public String getDdl() {
        return ddl;
    }

    public String getType() {
        return type;
    }

    public Table withDdl(String ddl) {
        this.ddl = ddl;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<name=" + name + ">";
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

    public boolean isTable() {
        return Objects.equals("TABLE", type);
    }

    public void addForeignKey(ForeignKey fk) {
        importedKeys.add(fk);
    }

    public static List<Table> filterTables(List<Table> tables, boolean isTable) {
        return tables.stream().filter(t -> t.isTable() == isTable).collect(Collectors.toList());
    }

    public boolean isKodeliste() {
        return "KODELISTE".equalsIgnoreCase(name);
    }

    public boolean isKodeverk() {
        return matchPrimaryKey(Kodeverk.KODEVERK_PK_SPEC);
    }

    @Override
    public void apply(int sectionLevel, MarkupDocBuilder doc) {
        doc.sectionTitleLevel(sectionLevel + 1, getName().toUpperCase());
        writeTableComment(doc);

        List<MarkupTableColumn> columnSpecs = new ArrayList<>();
        columnSpecs.add(new MarkupTableColumn("#", true, 5));
        columnSpecs.add(new MarkupTableColumn("Navn", true, 10));
        columnSpecs.add(new MarkupTableColumn("Type", false, 5));
        columnSpecs.add(new MarkupTableColumn("Default", false, 10));
        columnSpecs.add(new MarkupTableColumn("Nullable", false, 5));
        columnSpecs.add(new MarkupTableColumn("Constraint", false, 15));
        columnSpecs.add(new MarkupTableColumn("Comment", false, 30));
        columnSpecs.add(new MarkupTableColumn("Key", false, 20));

        final List<java.util.List<String>> cells = new ArrayList<>();
        final AtomicInteger i = new AtomicInteger();
        columns.forEach(c -> {
            cells.add(Arrays.asList(
                    String.valueOf(i.incrementAndGet()), c.getName(), c.getType(), c.getDefaultValue(), c.isNullable() ? "X" : "", "",
                    c.getComment(), getKeyText(c)));
        });

        if (cells.isEmpty()) {
            cells.add(Collections.nCopies(columnSpecs.size(), ""));
        }
        doc.tableWithColumnSpecs(columnSpecs, cells);
    }

    private String getKeyText(Column c) {
        StringBuilder sb = new StringBuilder();
        if (c.isPrimaryKey()) {
            sb.append("PK");
        }
        if (c.isForeignKey()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(c.getForeignKeyDefinition());
        }
        return sb.toString();
    }

    public void writeTableComment(MarkupDocBuilder doc) {
        doc.block(comment == null
                || comment.isEmpty()
                ? "<MISSING DOCUMENTATION>" : comment, MarkupBlockStyle.LITERAL);
    }

    public boolean matchPrimaryKey(String pkName) {
        boolean match = false;
        for (Column c : columns) {
            if (c.isPrimaryKey() && Objects.equals(c.getName(), pkName)) {
                /** Støtter kun tabeller med en PK kolonne som matcher. */
                match = true;
                break;
            }
        }

        return match;
    }

    public void addRow(List<String> row, boolean isHeader) {
        if (isHeader) {
            data.add(0, row);
        } else {
            data.add(row);
        }
    }
    
    public List<List<String>> getRows(){
        return data;
    }

    public List<Column> getColumnsExcept(Set<String> unntakKolonner) {
        Comparator<Column> comp = primaryKeyFirstComparator();
        return columns.stream().sorted(comp).filter(n -> !unntakKolonner.contains(n.getName())).collect(Collectors.toList());
    }

    private static Comparator<Column> primaryKeyFirstComparator() {
        Comparator<Column> comp = (c1, c2) -> Boolean.valueOf(c1.isPrimaryKey()).compareTo(c2.isPrimaryKey());
        return comp;
    }
}
