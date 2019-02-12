package no.nav.vedtak.felles.testutilities.cdi;

import javax.enterprise.context.ApplicationScoped;

import no.nav.vedtak.felles.testutilities.UnitTestConfiguration;
import no.nav.vedtak.konfig.PropertiesKonfigVerdiProvider;

/**
 * Tilgang til konfigurerbare verdier som er spesielt satt opp for enhetstester.
 * Brukes normalt for JUnit Integrasjonstester.
 */
@ApplicationScoped
public class UnitTestKonfigVerdiProvider extends PropertiesKonfigVerdiProvider {

    UnitTestKonfigVerdiProvider() {
        super(UnitTestConfiguration.getUnitTestProperties());
    }

    @Override
    public int getPrioritet() {
        return 1;
    }
}