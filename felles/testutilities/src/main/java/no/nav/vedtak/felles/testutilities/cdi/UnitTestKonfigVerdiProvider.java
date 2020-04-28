package no.nav.vedtak.felles.testutilities.cdi;

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import no.nav.vedtak.felles.testutilities.UnitTestConfiguration;
import no.nav.vedtak.konfig.PropertiesKonfigVerdiProvider;
import no.nav.vedtak.konfig.StandardPropertySource;

/**
 * Tilgang til konfigurerbare verdier som er spesielt satt opp for enhetstester.
 * Brukes normalt for JUnit Integrasjonstester.
 */
@ApplicationScoped
public class UnitTestKonfigVerdiProvider extends PropertiesKonfigVerdiProvider {

    static class Init {
        // lazy load singleton
        static final Properties PROPS = UnitTestConfiguration.getUnitTestProperties();
    }

    UnitTestKonfigVerdiProvider() {
        super(Init.PROPS, StandardPropertySource.APP_PROPERTIES);
    }

    @Override
    public int getPrioritet() {
        return 1;
    }
}