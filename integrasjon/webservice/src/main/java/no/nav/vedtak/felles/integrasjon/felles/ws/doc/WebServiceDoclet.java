package no.nav.vedtak.felles.integrasjon.felles.ws.doc;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import javax.jws.WebService;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebService;

public class WebServiceDoclet implements Doclet {

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
        WebServicesModell resultat = new WebServicesModell();

        Set<TypeElement> types = ElementFilter.typesIn(docEnv.getIncludedElements());
        Stream<TypeElement> feilFilter = types.stream().filter(te -> te.getAnnotation(SoapWebService.class) != null);

        feilFilter.forEach(te -> {
            String qualifiedName = te.getQualifiedName().toString();
            String comment = docEnv.getElementUtils().getDocComment(te);
            SoapWebService soapWebService = te.getAnnotation(SoapWebService.class);
            WebService webService = te.getAnnotation(WebService.class);
            resultat.leggTil(qualifiedName, comment, soapWebService, webService);
        });

        File outputFile = new File(getOutputLocation(), "eksponerteTjenester");
        new AsciidocMapper().writeTo(outputFile.toPath(), resultat);
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
