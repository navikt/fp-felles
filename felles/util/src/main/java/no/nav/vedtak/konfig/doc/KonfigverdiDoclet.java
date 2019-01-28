package no.nav.vedtak.konfig.doc;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import no.nav.vedtak.konfig.KonfigVerdi;

public class KonfigverdiDoclet implements Doclet {

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
    public boolean run(DocletEnvironment environment) {
        System.out.println("Kjører Javadoc Doclet - " + getClass().getSimpleName());
        try {
            doRun(environment);
            return true;
        } catch (Error | RuntimeException e) {
            reporter.print(Kind.ERROR, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void doRun(DocletEnvironment docEnv) {
        KonfigVerdiModell resultat = new KonfigVerdiModell();

        Set<TypeElement> types = ElementFilter.typesIn(docEnv.getIncludedElements());

        leggTilConstructorKonfigVerdiParametere(resultat, types);
        leggTilFieldKonfigVerdiParametere(resultat, types);

        File outputFile = new File(getOutputLocation(), "konfig"); //$NON-NLS-1$
        new AsciidocMapper().writeTo(outputFile.toPath(), resultat);
    }

    private void leggTilFieldKonfigVerdiParametere(KonfigVerdiModell resultat, Set<TypeElement> types) {
        Stream<VariableElement> fieldsKonfigVerdi = types.stream().flatMap(t -> ElementFilter.fieldsIn(t.getEnclosedElements()).stream())
            .filter(p -> p.getAnnotation(KonfigVerdi.class) != null);

        fieldsKonfigVerdi.forEach(ve -> {
            TypeElement cls = (TypeElement) ve.getEnclosingElement();
            String qualifiedName = cls.getQualifiedName().toString();
            resultat.leggTil(qualifiedName, ve.getAnnotation(KonfigVerdi.class));
        });
    }

    private void leggTilConstructorKonfigVerdiParametere(KonfigVerdiModell resultat, Set<TypeElement> types) {
        Stream<? extends VariableElement> constructorKonfigVerdi = types.stream().flatMap(t -> ElementFilter.constructorsIn(t.getEnclosedElements()).stream())
            .flatMap(ee -> ee.getParameters().stream())
            .filter(p -> p.getAnnotation(KonfigVerdi.class) != null);

        constructorKonfigVerdi.forEach(ve -> {
            Element ctor = ve.getEnclosingElement();
            TypeElement cls = (TypeElement) ctor.getEnclosingElement();
            String qualifiedName = cls.getQualifiedName().toString();
            resultat.leggTil(qualifiedName, ve.getAnnotation(KonfigVerdi.class));
        });
    }

    private File getOutputLocation() {
        File dir = new File(System.getProperty("destDir", "target/docs")); //$NON-NLS-1$ //$NON-NLS-2$
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("Could not create output directory:" + dir); //$NON-NLS-1$
            }
        }
        return dir;
    }

}
