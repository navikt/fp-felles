package no.nav.vedtak.konfig;

import javax.enterprise.context.ApplicationScoped;

/** Henter properties fra {@link System#getProperties}. */
@ApplicationScoped
public class SystemPropertiesKonfigVerdiProvider extends PropertiesKonfigVerdiProvider {
    
    public SystemPropertiesKonfigVerdiProvider() {
        super(System.getProperties());
    }

    @Override
    public int getPrioritet() {
        return 10; // NOSONAR
    }
}
