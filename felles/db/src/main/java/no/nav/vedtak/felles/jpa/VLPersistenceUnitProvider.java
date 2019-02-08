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

import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

/**
 * Implementation of {@link PersistenceProvider} which loads all mapping files dynamically from classpath as long as they match the pattern
 * &lt;persistence-unit-name&gt;.xxx.orm.xml).
 * 
 * Makes it simpler to support entities in different modules without having to be on build-time dependency path of where persistence.xml is.
 */
@SuppressWarnings("rawtypes")
public class VLPersistenceUnitProvider extends HibernatePersistenceProvider {

    public VLPersistenceUnitProvider() {
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
            
            AdditionalMappingFilesPersistenceUnitDescriptor newPud = new AdditionalMappingFilesPersistenceUnitDescriptor(pud);
            newPud.addMappingFileNames(ormFiles);
            return newPud;
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException("Could not load ORM files for persistence unit " + pud.getName(), e);
        }
    }

    /** Scan a named folder present in jars or directories on classpath for files matching a pattern. */
    static Set<String> getResourceFolderFiles(String folder, Pattern filePattern) throws IOException, URISyntaxException {
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