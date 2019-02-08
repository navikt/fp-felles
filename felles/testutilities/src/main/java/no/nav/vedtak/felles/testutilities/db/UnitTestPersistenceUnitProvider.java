package no.nav.vedtak.felles.testutilities.db;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;

import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.jpa.DelegatingPersistenceUnitDescriptor;
import no.nav.vedtak.felles.jpa.VLPersistenceUnitProvider;
import no.nav.vedtak.felles.testutilities.VariablePlaceholderReplacer;

public class UnitTestPersistenceUnitProvider extends VLPersistenceUnitProvider {

    public UnitTestPersistenceUnitProvider() {
    }

    @Override
    protected PersistenceUnitDescriptor extendPersistenceUnitDescriptor(PersistenceUnitDescriptor pud) {
        return new InjectingPersistenceUnitDescriptor(super.extendPersistenceUnitDescriptor(pud));
    }
    
    @Override
    protected boolean isMatchingProvider(ParsedPersistenceXmlDescriptor persistenceUnit, @SuppressWarnings("rawtypes") Map properties) {
        // Går alltid for gull
        return true;  
    }

    /** Injecter properties i orm (mapping-files) filer. */
    static class InjectingPersistenceUnitDescriptor extends DelegatingPersistenceUnitDescriptor {
        private static final Logger log = LoggerFactory.getLogger(InjectingPersistenceUnitDescriptor.class);
        private final List<String> mappingFiles = new ArrayList<>();
        private final VariablePlaceholderReplacer replacePlaceholder;

        public InjectingPersistenceUnitDescriptor(PersistenceUnitDescriptor persistenceUnitDescriptor) {
            super(persistenceUnitDescriptor);

            replacePlaceholder = new VariablePlaceholderReplacer(System.getProperties());

            for (String mf : persistenceUnitDescriptor.getMappingFileNames()) {
                String injectedFile = injectFile(mf);
                this.mappingFiles.add(injectedFile);
            }
        }

        private String injectFile(String mf) {
            String newFile = mf + ".unittest";

            String content;
            try {
                content = readContent(mf);

                String replaceContent = replacePlaceholder.replacePlaceholders(content);
                replaceContent = simpleReplacement(replaceContent);

                if (Objects.equals(content, replaceContent)) {
                    return mf;
                } else {
                    // skriv ny fil og endre path
                    Path newFilePath = getTempWritePath(newFile);
                    Files.createDirectories(newFilePath.getParent());
                    Files.write(newFilePath, replaceContent.getBytes());
                    URI uri = newFilePath.toAbsolutePath().toUri();
                    String uriAscii = uri.toASCIIString();

                    log.debug("Overstyrt mapping-file [{}] til {}", mf, uriAscii);

                    return uriAscii;
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Kunne ikke lese eller konvertere mapping-file: " + mf, e);
            }
        }

        private Path getTempWritePath(String newFile) throws IOException {
            Path dir = Files.createTempDirectory("tmp");
            return dir.resolve(newFile);
        }

        private String readContent(String inputFile) throws IOException {
            if (inputFile.startsWith("META-INF")) {
                inputFile = "/" + inputFile;
            }
            try (InputStream is = getClass().getResourceAsStream(inputFile);
                    Scanner scan = new Scanner(is, "UTF-8")) {
                scan.useDelimiter("\\Z");
                return scan.next();
            }
        }

        private String simpleReplacement(final String content) {

            Map<String, String> replacements = new LinkedHashMap<>();
            if (System.getProperty("flyway.placeholders.vl_fpsak_hist_schema_unit") != null) {
                replacements.putIfAbsent("\\bFPSAK_HIST\\b", System.getProperty("flyway.placeholders.vl_fpsak_hist_schema_unit"));
            }
            if (System.getProperty("flyway.placeholders.vl_fpsak_schema_unit") != null) {
                replacements.putIfAbsent("\\bFPSAK\\b", System.getProperty("flyway.placeholders.vl_fpsak_schema_unit"));
            }

            return simpleReplacement(content, replacements);
        }

        private String simpleReplacement(final String content, final Map<String, String> replacements) {
            String newContent = content;
            for (Map.Entry<String, String> replacement : replacements.entrySet()) {
                newContent = newContent.replaceAll(replacement.getKey(), Matcher.quoteReplacement(replacement.getValue()));
            }
            return newContent;
        }

        @Override
        public List<String> getMappingFileNames() {
            return Collections.unmodifiableList(mappingFiles);
        }

    }

    
}
