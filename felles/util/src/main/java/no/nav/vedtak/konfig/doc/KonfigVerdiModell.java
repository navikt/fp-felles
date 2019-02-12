package no.nav.vedtak.konfig.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupTableColumn;
import no.nav.vedtak.konfig.KonfigVerdi;

public class KonfigVerdiModell implements MarkupOutput {

    static class Entry {
        String targetClassQualifiedName;
        KonfigVerdi annotation;

        Entry(String targetClass, KonfigVerdi annotation) {
            this.targetClassQualifiedName = targetClass;
            this.annotation = annotation;
        }

    }

    private final List<Entry> entries = new ArrayList<>();

    @Override
    public void apply(int sectionLevel, MarkupDocBuilder doc) {

       ResourceBundle bundle = ResourceBundle.getBundle("konfig-beskrivelser");

        List<MarkupTableColumn> columnSpecs = new ArrayList<>();
        columnSpecs.add(new MarkupTableColumn("Konfig-nøkkel", true, 15));
        columnSpecs.add(new MarkupTableColumn("Beskrivelse", false, 20));
        columnSpecs.add(new MarkupTableColumn("Bruk", false, 20));

        final List<java.util.List<String>> cells = new ArrayList<>();
        for (Entry entry : entries) {
            String key = entry.annotation.value();
            
            if(!bundle.containsKey(key)) {
                System.out.println("mangler beskrivelse for : " + key);
                continue;
            }

            String beskrivelse = bundle.getString(key);
            List<String> data = Arrays.asList(
                key,
                beskrivelse,
                entry.targetClassQualifiedName);

            List<String> rowNoNulls = data
                .stream()
                .map(c -> c == null ? "" : c)
                .collect(Collectors.toList());
            cells.add(rowNoNulls);
        }

        if (cells.isEmpty()) {
            cells.add(Collections.nCopies(columnSpecs.size(), ""));
        }
        doc.tableWithColumnSpecs(columnSpecs, cells);
    }

    public void leggTil(String targetClass, KonfigVerdi annotation) {
        this.entries.add(new Entry(targetClass, annotation));
    }

}
