package no.nav.vedtak.konfig;

import static no.nav.vedtak.konfig.StandardPropertySource.SYSTEM_PROPERTIES;

import javax.enterprise.context.ApplicationScoped;

/** Henter properties fra {@link System#getProperties}. */
@ApplicationScoped
public class SystemPropertiesKonfigVerdiProvider extends PropertiesKonfigVerdiProvider {
    public static final int PRIORITET = Integer.MIN_VALUE;

    public SystemPropertiesKonfigVerdiProvider() {
        super(System.getProperties(), SYSTEM_PROPERTIES);
    }

    @Override
    public int getPrioritet() {
        return PRIORITET;
    }
}
