package no.nav.vedtak.felles.jpa;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

import org.hibernate.bytecode.enhance.spi.EnhancementContext;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.jpa.internal.enhance.EnhancingClassTransformerImpl;

/**
 * Bridging calls to PersistenceUnitDescriptor onto a PersistenceUnitInfo implementation.
 */
class PersistenceUnitInfoDescriptorAdapter implements PersistenceUnitDescriptor {
    private final PersistenceUnitInfo persistenceUnitInfo;

    public PersistenceUnitInfoDescriptorAdapter(PersistenceUnitInfo persistenceUnitInfo) {
        this.persistenceUnitInfo = persistenceUnitInfo;
    }

    @Override
    public ClassLoader getClassLoader() {
        return persistenceUnitInfo.getClassLoader();
    }

    @Override
    public List<URL> getJarFileUrls() {
        return persistenceUnitInfo.getJarFileUrls();
    }

    @Override
    public Object getJtaDataSource() {
        return persistenceUnitInfo.getJtaDataSource();
    }

    @Override
    public List<String> getManagedClassNames() {
        return persistenceUnitInfo.getManagedClassNames();
    }

    @Override
    public List<String> getMappingFileNames() {
        return persistenceUnitInfo.getMappingFileNames();
    }

    @Override
    public String getName() {
        return persistenceUnitInfo.getPersistenceUnitName();
    }

    @Override
    public Object getNonJtaDataSource() {
        return persistenceUnitInfo.getNonJtaDataSource();
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitInfo.getPersistenceUnitRootUrl();
    }

    @Override
    public Properties getProperties() {
        return persistenceUnitInfo.getProperties();
    }

    @Override
    public String getProviderClassName() {
        return persistenceUnitInfo.getPersistenceProviderClassName();
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return persistenceUnitInfo.getSharedCacheMode();
    }

    @Override
    public ClassLoader getTempClassLoader() {
        return persistenceUnitInfo.getNewTempClassLoader();
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return persistenceUnitInfo.getTransactionType();
    }

    @Override
    public ValidationMode getValidationMode() {
        return persistenceUnitInfo.getValidationMode();
    }

    @Override
    public boolean isExcludeUnlistedClasses() {
        return persistenceUnitInfo.excludeUnlistedClasses();
    }

    @Override
    public boolean isUseQuotedIdentifiers() {
        return false;
    }

    @Override
    public void pushClassTransformer(EnhancementContext enhancementContext) {
        persistenceUnitInfo.addTransformer(new EnhancingClassTransformerImpl(enhancementContext));
    }
}
