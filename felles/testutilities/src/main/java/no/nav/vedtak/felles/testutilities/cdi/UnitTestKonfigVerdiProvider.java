package no.nav.vedtak.felles.testutilities.cdi;

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

    UnitTestKonfigVerdiProvider() {
        super(UnitTestConfiguration.getUnitTestProperties(), StandardPropertySource.APP_PROPERTIES);
    }

    @Override
    public int getPrioritet() {
        return 1;
    }
}