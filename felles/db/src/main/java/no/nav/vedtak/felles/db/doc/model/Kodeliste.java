package no.nav.vedtak.felles.db.doc.model;

import io.github.swagger2markup.markup.builder.MarkupBlockStyle;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Kodeliste implements MarkupOutput {

    private final Collection<Table> tables;
    private final Table kodeverkTable;

    public Kodeliste(Table table) {
        tables = splitTable(table);
        kodeverkTable = table;
    }

    @SuppressWarnings("unchecked")
    private Collection<Table> splitTable(Table table) {
        Map<String, Table> tableMap = new HashMap<>();
        final AtomicInteger i = new AtomicInteger();

        table.getRows().forEach(r -> {
            if (i.incrementAndGet() > 1) { // skip header
                String kodelisteDelTableName = r.get(0);
                if (tableMap.get(kodelisteDelTableName) == null) {
                    Table kodelisteSub = new Table(kodelisteDelTableName, table.getType(), "", true);
                    tableMap.put(kodelisteDelTableName, kodelisteSub);
                    for (Column c : table.getColumnsExcept(Kodeverk.KODEVERK_UNNTAK_KOLONNER)) {
                        kodelisteSub.addColumn(c);
                    }
                }

                Table kodelisteSub = tableMap.get(kodelisteDelTableName);
                kodelisteSub.addRow(r, false);
            }
        });

        @SuppressWarnings("rawtypes")
        Map result = tableMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return result.values();
    }

    @Override
    public void apply(int sectionLevel, MarkupDocBuilder doc) {
        doc.block(kodeverkTable.getComment(), MarkupBlockStyle.LITERAL);
        for (Table t: tables) {
            new Kodeverk(t).apply(sectionLevel, doc);
        }
    }
}
