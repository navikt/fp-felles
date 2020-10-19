package no.nav.vedtak.konfig.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupTableColumn;
import no.nav.vedtak.konfig.KonfigVerdi;

public class KonfigVerdiModell implements MarkupOutput {

    private static final Logger log = LoggerFactory.getLogger(KonfigVerdiModell.class);

    static class Entry {
        String targetClassQualifiedName;
        KonfigVerdi annotation;

        Entry(String targetClass, KonfigVerdi annotation) {
            this.targetClassQualifiedName = targetClass;
            this.annotation = annotation;
        }

    }

    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, String> beskrivelser = new HashMap<>();

    @Override
    public void apply(int sectionLevel, MarkupDocBuilder doc) {

        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("konfig-beskrivelser");
        } catch (MissingResourceException e) {
            log.warn("Fant ikke resource bundle", e);
            bundle = null;
        }

        List<MarkupTableColumn> columnSpecs = new ArrayList<>();
        columnSpecs.add(new MarkupTableColumn("Konfig-nøkkel", true, 15));
        columnSpecs.add(new MarkupTableColumn("Beskrivelse", false, 20));
        columnSpecs.add(new MarkupTableColumn("Bruk", false, 20));

        final List<java.util.List<String>> cells = new ArrayList<>();
        for (Entry entry : entries) {
            String key = entry.annotation.value();

            String beskrivelse;
            if (beskrivelser.containsKey(key)) {
                beskrivelse = beskrivelser.get(key);
            } else if (bundle == null || !bundle.containsKey(key)) {
                log.warn("mangler beskrivelse for {}", key);
                continue;
            } else {
                beskrivelse = bundle.getString(key);
            }

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
        if (!annotation.beskrivelse().isBlank()) {
            this.beskrivelser.put(annotation.value(), annotation.beskrivelse());
        }
    }

}
