package no.nav.foreldrepenger.konfig;

import javax.enterprise.context.Dependent;

import static no.nav.foreldrepenger.konfig.StandardPropertySource.SYSTEM_PROPERTIES;

/**
 * Henter properties fra {@link System#getProperties}.
 */
@Dependent
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
