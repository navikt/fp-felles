package no.nav.vedtak.konfig.doc;

import java.io.File;

import javax.tools.DiagnosticCollector;
import javax.tools.DocumentationTool;
import javax.tools.DocumentationTool.DocumentationTask;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

public class KonfigverdiDocletTest {
    @Test
    public void test_generer_javadoc_for_WebService() throws Exception {
        DocumentationTool documentationTool = ToolProvider.getSystemDocumentationTool();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        try (StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null)) {
            Iterable<? extends JavaFileObject> javaFileObjects = fm
                    .getJavaFileObjects(new File("src/test/java/no/nav/vedtak/konfig/doc/DummyKonfigVerdi.java"));
            DocumentationTask task = documentationTool.getTask(null, fm, null, KonfigverdiDoclet.class, null, javaFileObjects);

            task.call();
        }
    }
}
