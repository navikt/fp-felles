package no.nav.vedtak.felles.testutilities.db;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
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

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.VariablePlaceholderReplacer;

/**
 * En {@link HibernatePersistenceProvider} implementasjon som støtter å laste persistence.xml fra en alternativ
 * lokasjon.
 * <p>
 * Support for å laste alternative persistence.xml filer under testing for å unngå at hibernate bundler flere sammen
 */
@SuppressWarnings("rawtypes")
public class UnitTestPersistenceUnitProvider extends HibernatePersistenceProvider {

    private static final Logger log = LoggerFactory.getLogger(UnitTestPersistenceUnitProvider.class);
    
    private final URL altPersistenceXmlUrl;

    public UnitTestPersistenceUnitProvider() {
        this.altPersistenceXmlUrl = null;
    }

    public UnitTestPersistenceUnitProvider(URL persistenceXml) {
        this.altPersistenceXmlUrl = persistenceXml;
    }

    public UnitTestPersistenceUnitProvider(String classpathPersistenceUnit) {
        this.altPersistenceXmlUrl = classpathPersistenceUnit == null ? null : this.getClass().getResource(classpathPersistenceUnit);
    }

    @Override
    public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map map) {
        final Map integration = wrap(map);

        final PersistenceUnitDescriptor unit;
        try {
            unit = injectProperties(persistenceUnitName, integration);
        } catch (Exception e) {
            throw new PersistenceException("Unable to locate persistence units", e);
        }
        return Bootstrap.getEntityManagerFactoryBuilder(unit, integration).build();
    }

    private PersistenceUnitDescriptor injectProperties(String persistenceUnitName, final Map integration) throws IOException {
        PersistenceUnitDescriptor persistenceUnit = getPersistenceUnit(persistenceUnitName, integration);
        return new InjectingPersistenceUnitDescriptor(persistenceUnit);
    }

    protected ParsedPersistenceXmlDescriptor getPersistenceUnit(final String persistenceUnitName, final Map integration) {
        if (altPersistenceXmlUrl != null) {
            return PersistenceXmlParser.locateIndividualPersistenceUnit(altPersistenceXmlUrl, integration);
        } else {
            List<ParsedPersistenceXmlDescriptor> persistenceUnits = PersistenceXmlParser.locatePersistenceUnits(integration);
            for (ParsedPersistenceXmlDescriptor pu : persistenceUnits) {
                if (pu.getName().equals(persistenceUnitName)) {
                    return pu;
                }
            }
        }
        throw new IllegalArgumentException("Persistence unit: " + persistenceUnitName + " not found");
    }

    @Override
    public void generateSchema(PersistenceUnitInfo info, Map map) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean generateSchema(String persistenceUnitName, Map map) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Injecter properties i orm (mapping-files) filer. */
    static class InjectingPersistenceUnitDescriptor extends DelegatingPersistenceUnitDescriptor {

        private final List<String> mappingFiles = new ArrayList<>();
        private final VariablePlaceholderReplacer replacePlaceholder;

        public InjectingPersistenceUnitDescriptor(PersistenceUnitDescriptor persistenceUnitDescriptor) throws IOException {
            super(persistenceUnitDescriptor);

            replacePlaceholder = new VariablePlaceholderReplacer(System.getProperties());

            for (String mf : persistenceUnitDescriptor.getMappingFileNames()) {
                String injectedFile = injectFile(mf);
                this.mappingFiles.add(injectedFile);
            }
        }

        private String injectFile(String mf) throws IOException {
            String newFile = mf + ".unittest";

            String content = readContent(mf);
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
