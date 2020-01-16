package no.nav.vedtak.feil.doc;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;

public class FeilmeldingDoclet implements Doclet {

    @SuppressWarnings("unused")
    private Locale locale;
    private Reporter reporter;

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.locale = locale;
        this.reporter = reporter;

    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Collections.emptySet();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_10;
    }

    @Override
    public boolean run(DocletEnvironment docEnv) {
        System.out.println("Kjører Javadoc Doclet - " + getClass().getSimpleName());
        FeilmeldingModell resultat = new FeilmeldingModell(docEnv);

        Set<TypeElement> types = ElementFilter.typesIn(docEnv.getIncludedElements());
        Stream<TypeElement> feilFilter = types.stream().filter(te -> te.getInterfaces().stream()
            .anyMatch(tei -> ((DeclaredType) tei).asElement().getSimpleName().toString().equals(DeklarerteFeil.class.getSimpleName())));
        feilFilter.forEach(te -> resultat.leggTil(te));

        try {
            File outputFile = new File(getOutputLocation(), "feil");
            new AsciidocMapper().writeTo(outputFile.toPath(), resultat);
            return true;
        } catch (Error | RuntimeException e) {
            reporter.print(Kind.ERROR, e.getMessage());
            e.printStackTrace(); // NOSONAR
        }
        return false;
    }

    private File getOutputLocation() {
        File dir = new File(System.getProperty("destDir", "target/docs"));
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("Could not create output directory:" + dir);
            }
        }
        return dir;
    }

}
