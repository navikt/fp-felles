package no.nav.vedtak.felles.feil;

import java.util.Collections;
import java.util.List;

import no.nav.vedtak.feil.AbstractFeilTestIT;

public class DeklarativeFeilTestIT extends AbstractFeilTestIT {
    @Override
    protected List<String> getForventedePrefix() {
        return Collections.singletonList("F");
    }
}
