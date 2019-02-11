package no.nav.vedtak.felles.jpa;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.jpa.boot.spi.ProviderChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link PersistenceProvider} which loads all mapping files dynamically from classpath as long as they match the pattern
 * &lt;persistence-unit-name&gt;.xxx.orm.xml).  They must reside directly under META-INF/.
 * 
 * Makes it simpler to support entities in different modules without having to be on build-time dependency path of where persistence.xml is.
 */
@SuppressWarnings("rawtypes")
public class VLPersistenceUnitProvider extends HibernatePersistenceProvider {

    private static final Logger log = LoggerFactory.getLogger(VLPersistenceUnitProvider.class);

    public VLPersistenceUnitProvider() {
    }

    /** Add additional mapping files based on naming convention. */
    protected PersistenceUnitDescriptor extendPersistenceUnitDescriptor(PersistenceUnitDescriptor pud) {
        class AdditionalMappingFilesPersistenceUnitDescriptor extends DelegatingPersistenceUnitDescriptor {
            private final List<String> mappingFiles = new ArrayList<>();

            AdditionalMappingFilesPersistenceUnitDescriptor(PersistenceUnitDescriptor pud) {
                super(pud);
                mappingFiles.addAll(pud.getMappingFileNames());
            }

            @Override
            public List<String> getMappingFileNames() {
                return this.mappingFiles;
            }

            void addMappingFileNames(Collection<String> mappingFileNames) {
                this.mappingFiles.addAll(mappingFileNames);
            }
        }

        try {
            Pattern ormPattern = Pattern.compile("^META-INF/" + pud.getName() + "\\..+\\.orm\\.xml$");
            Set<String> ormFiles = getResourceFolderFiles("META-INF", ormPattern);
            ormFiles.forEach(f -> log.info("Found ORM mapping file: " + f));
            
            AdditionalMappingFilesPersistenceUnitDescriptor newPud = new AdditionalMappingFilesPersistenceUnitDescriptor(pud);
            newPud.addMappingFileNames(ormFiles);
            return newPud;
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException("Could not load ORM files for persistence unit " + pud.getName(), e);
        }
    }
    

    @Override
    public void generateSchema(PersistenceUnitInfo info, Map map) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean generateSchema(String persistenceUnitName, Map map) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilder(PersistenceUnitDescriptor persistenceUnitDescriptor, Map integration,
                                                                         ClassLoader providedClassLoader) {
        return super.getEntityManagerFactoryBuilder(extendPersistenceUnitDescriptor(persistenceUnitDescriptor), integration, providedClassLoader);
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilder(PersistenceUnitDescriptor persistenceUnitDescriptor, Map integration,
                                                                         ClassLoaderService providedClassLoaderService) {
        return super.getEntityManagerFactoryBuilder(extendPersistenceUnitDescriptor(persistenceUnitDescriptor), integration, providedClassLoaderService);
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilder(PersistenceUnitInfo info, Map integration) {
        PersistenceUnitInfoDescriptorAdapter persistenceUnitDescriptor = new PersistenceUnitInfoDescriptorAdapter(info);
        return getEntityManagerFactoryBuilder(extendPersistenceUnitDescriptor(persistenceUnitDescriptor), integration, (ClassLoader) null);
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map properties) {
        // duplisert fra HibernatePersistenceProvider for å kunne ha egen implementasjon av getEntityManagerFactoryBuilderOrNull
        return getEntityManagerFactoryBuilderOrNull(persistenceUnitName, properties, null, null);
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map properties,
                                                                               ClassLoader providedClassLoader) {
        // duplisert fra HibernatePersistenceProvider for å kunne ha egen implementasjon av getEntityManagerFactoryBuilderOrNull
        return getEntityManagerFactoryBuilderOrNull(persistenceUnitName, properties, providedClassLoader, null);
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map properties,
                                                                               ClassLoaderService providedClassLoaderService) {
        // duplisert fra HibernatePersistenceProvider for å kunne ha egen implementasjon av getEntityManagerFactoryBuilderOrNull
        return getEntityManagerFactoryBuilderOrNull(persistenceUnitName, properties, null, providedClassLoaderService);
    }

    private EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map properties,
                                                                             ClassLoader providedClassLoader, ClassLoaderService providedClassLoaderService) {

        // duplisert fra HibernatePersistenceProvider for å kunne overstyre kall til ProviderChecker (siden den hardkoder
        // HibernatePersistenceProvider klassenavn)
        
        log.trace("Attempting to obtain correct EntityManagerFactoryBuilder for persistenceUnitName : %s", persistenceUnitName);

        final Map integration = wrap(properties);
        final List<ParsedPersistenceXmlDescriptor> units;
        try {
            units = PersistenceXmlParser.locatePersistenceUnits(integration);
        } catch (Exception e) {
            log.debug("Unable to locate persistence units", e);
            throw new PersistenceException("Unable to locate persistence units", e);
        }

        log.debug("Located and parsed %s persistence units; checking each", units.size());

        if (persistenceUnitName == null && units.size() > 1) {
            // no persistence-unit name to look for was given and we found multiple persistence-units
            throw new PersistenceException("No name provided and multiple persistence units found");
        }

        for (ParsedPersistenceXmlDescriptor persistenceUnit : units) {
            log.debug(
                "Checking persistence-unit [name=%s, explicit-provider=%s] against incoming persistence unit name [%s]",
                persistenceUnit.getName(),
                persistenceUnit.getProviderClassName(),
                persistenceUnitName);

            final boolean matches = persistenceUnitName == null || persistenceUnit.getName().equals(persistenceUnitName);
            if (!matches) {
                log.debug("Excluding from consideration due to name mis-match");
                continue;
            }

            if(!isMatchingProvider(persistenceUnit, properties)) {
                log.debug("Excluding from consideration due to provider mis-match");
                continue;
            }
           
            if (providedClassLoaderService != null) {
                return getEntityManagerFactoryBuilder(persistenceUnit, integration, providedClassLoaderService);
            } else {
                return getEntityManagerFactoryBuilder(persistenceUnit, integration, providedClassLoader);
            }
        }

        log.debug("Found no matching persistence units");
        return null;
    }

    /** overridden check from HibernatePersistenceProvider */
    protected boolean isMatchingProvider(ParsedPersistenceXmlDescriptor persistenceUnit, Map properties) {
        /*
        // See if we (Hibernate) are the persistence provider
        if (!ProviderChecker.isProvider(persistenceUnit, properties)) {
            log.debug("Excluding from consideration due to provider mis-match");
            continue;
        }
        */
        
        // Alternativ 
        String requestedProviderName = ProviderChecker.extractRequestedProviderName(persistenceUnit, properties);
        return getClass().getName().equals(requestedProviderName);
    }


    /** Scan a named folder present in jars or directories on classpath for files matching a pattern. */
    Set<String> getResourceFolderFiles(String folder, Pattern filePattern) throws IOException, URISyntaxException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> en = loader.getResources(folder);

        Set<String> relativeFilenames = new TreeSet<>();
        for (Iterator<URL> it = en.asIterator(); it.hasNext();) {
            URL url = it.next();
            List<String> filenames = new ArrayList<String>();
            if (url != null) {
                if (url.getProtocol().equals("file")) {
                    Files.walk(Paths.get(url.toURI())).filter(Files::isRegularFile).forEach(filePath -> {
                        filenames.add(filePath.toFile().getAbsolutePath());
                    });
                } else if (url.getProtocol().equals("jar")) {
                    String dirname = folder + "/";
                    String path = url.getPath();
                    String jarPath = path.substring(5, path.indexOf("!"));
                    try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name()))) {
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.startsWith(dirname) && !dirname.equals(name) && !entry.isDirectory()) {
                                filenames.add(name);
                            }
                        }
                    }
                }
            }

            // clean up - relative path + folder separator
            filenames.forEach(f -> {
                String relativeFile = f.substring(f.indexOf(folder)).replace('\\', '/');
                if (filePattern.matcher(relativeFile).matches()) {
                    relativeFilenames.add(relativeFile);
                }
            });
        }

        return relativeFilenames;
    }

}