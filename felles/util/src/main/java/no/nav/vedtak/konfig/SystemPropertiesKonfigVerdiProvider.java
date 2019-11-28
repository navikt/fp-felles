package no.nav.vedtak.konfig;

import javax.enterprise.context.ApplicationScoped;

/** Henter properties fra {@link System#getProperties}. */
@ApplicationScoped
public class SystemPropertiesKonfigVerdiProvider extends PropertiesKonfigVerdiProvider {
    public static final int PRIORITET = Integer.MIN_VALUE;
    
    public SystemPropertiesKonfigVerdiProvider() {
        super(System.getProperties());
    }

    @Override
    public int getPrioritet() {
        return PRIORITET;
    }
}
