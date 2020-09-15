package no.nav.vedtak.feil.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;

import io.github.swagger2markup.markup.builder.MarkupBlockStyle;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupTableColumn;
import jdk.javadoc.doclet.DocletEnvironment;

public class FeilmeldingModell implements MarkupOutput {

    private final Comparator<TypeElement> comp = Comparator.comparing(e -> e.getQualifiedName().toString());

    private final NavigableSet<TypeElement> entries = new TreeSet<>(comp);

    private DocletEnvironment docEnv;

    public FeilmeldingModell(DocletEnvironment docEnv) {
        this.docEnv = docEnv;
    }

    @Override
    public void apply(int sectionLevel, MarkupDocBuilder doc) {
        for (TypeElement entry : entries) {
            String qualifiedName = entry.getQualifiedName().toString();

            String comment = docEnv.getElementUtils().getDocComment(entry);

            doc.sectionTitleLevel(sectionLevel, shortName(qualifiedName));
            if (comment != null && !comment.isEmpty()) {
                doc.block(comment, MarkupBlockStyle.LITERAL);
            }

            List<MarkupTableColumn> columnSpecs = new ArrayList<>();
            columnSpecs.add(new MarkupTableColumn("Feilkode", true, 5));
            columnSpecs.add(new MarkupTableColumn("Level", false, 5));
            columnSpecs.add(new MarkupTableColumn("Type", false, 5));
            columnSpecs.add(new MarkupTableColumn("Feilmelding", false, 20));
            columnSpecs.add(new MarkupTableColumn("Løsningsforslag", false, 15));
            columnSpecs.add(new MarkupTableColumn("Parametre", false, 15));
            columnSpecs.add(new MarkupTableColumn("Cause", false, 20));
            columnSpecs.add(new MarkupTableColumn("Java-metode", false, 15));

            final List<java.util.List<String>> cells = new ArrayList<>();

            buildCells(doc, qualifiedName, columnSpecs, cells);
        }
    }

    private static void buildCells(MarkupDocBuilder doc, String qualifiedName, List<MarkupTableColumn> columnSpecs,
            final List<java.util.List<String>> cells) {
        // TODO - dette kan skrives om til å bruke javax.lang.model direkte slik at en
        // slipper
        // å ha kompilerte klasser tilgjengelig for å få reflection.
        Class<?> targetCls;
        try {
            targetCls = Class.forName(qualifiedName);
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Fant ikke klasse, kan ikke generere doc uten tilgang til denne: " + qualifiedName, e);
        }

        Arrays.asList(targetCls.getMethods()).forEach(method -> {
            String løsningsforslag = FeilUtil.løsningsforslag(method);

            int antallVanligeParametre = FeilUtil.tellParametreUtenomCause(method);
            List<String> parametre = Arrays.asList(Arrays.copyOf(method.getParameters(), antallVanligeParametre)).stream()
                    .map(p -> p.getName() + ": " + p.getType().getSimpleName()).collect(Collectors.toList());

            String deklarertCause = FeilUtil.harMedCause(method) ? FeilUtil.deklarertCause(method).getSimpleName() : "";

            List<String> data = Arrays.asList(
                    FeilUtil.feilkode(method),
                    String.valueOf(FeilUtil.logLevel(method)),
                    FeilUtil.type(method),
                    FeilUtil.feilmelding(method),
                    løsningsforslag,
                    String.join(", ", parametre),
                    deklarertCause,
                    method.getName());
            List<String> rowNoNulls = data
                    .stream()
                    .map(c -> c == null ? "" : c)
                    .collect(Collectors.toList());
            cells.add(rowNoNulls);
        });

        if (cells.isEmpty()) {
            cells.add(Collections.nCopies(columnSpecs.size(), ""));
        }
        doc.tableWithColumnSpecs(columnSpecs, cells);
    }

    private static String shortName(String name) {
        // fjerner 3 første ledd i pakkenavn
        return name.replaceAll("^([a-z0-9]+\\.){3}", "");
    }

    public void leggTil(TypeElement e) {
        entries.add(e);
    }

}
