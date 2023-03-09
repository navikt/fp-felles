package no.nav.vedtak.felles.jpa;

import org.hibernate.bytecode.enhance.spi.EnhancementContext;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Delegerer kall fra en {@link PersistenceUnitDescriptor} til en annen slik at det er enklere å lage en SPI implementasjon baser på Hibernate
 */
public class DelegatingPersistenceUnitDescriptor implements PersistenceUnitDescriptor {
    private PersistenceUnitDescriptor persistenceUnitDescriptor;

    public DelegatingPersistenceUnitDescriptor(PersistenceUnitDescriptor persistenceUnitDescriptor) {
        this.persistenceUnitDescriptor = persistenceUnitDescriptor;
    }

    @Override
    public List<String> getMappingFileNames() {
        return persistenceUnitDescriptor.getMappingFileNames();
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitDescriptor.getPersistenceUnitRootUrl();
    }

    @Override
    public String getName() {
        return persistenceUnitDescriptor.getName();
    }

    @Override
    public String getProviderClassName() {
        return persistenceUnitDescriptor.getProviderClassName();
    }

    @Override
    public boolean isUseQuotedIdentifiers() {
        return persistenceUnitDescriptor.isUseQuotedIdentifiers();
    }

    @Override
    public boolean isExcludeUnlistedClasses() {
        return persistenceUnitDescriptor.isExcludeUnlistedClasses();
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return persistenceUnitDescriptor.getTransactionType();
    }

    @Override
    public ValidationMode getValidationMode() {
        return persistenceUnitDescriptor.getValidationMode();
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return persistenceUnitDescriptor.getSharedCacheMode();
    }

    @Override
    public List<String> getManagedClassNames() {
        return persistenceUnitDescriptor.getManagedClassNames();
    }

    @Override
    public List<URL> getJarFileUrls() {
        return persistenceUnitDescriptor.getJarFileUrls();
    }

    @Override
    public Object getNonJtaDataSource() {
        return persistenceUnitDescriptor.getNonJtaDataSource();
    }

    @Override
    public Object getJtaDataSource() {
        return persistenceUnitDescriptor.getJtaDataSource();
    }

    @Override
    public Properties getProperties() {
        return persistenceUnitDescriptor.getProperties();
    }

    @Override
    public ClassLoader getClassLoader() {
        return persistenceUnitDescriptor.getClassLoader();
    }

    @Override
    public ClassLoader getTempClassLoader() {
        return persistenceUnitDescriptor.getTempClassLoader();
    }

    @Override
    public void pushClassTransformer(EnhancementContext enhancementContext) {
        persistenceUnitDescriptor.pushClassTransformer(enhancementContext);
    }

}
