package no.nav.vedtak.felles.integrasjon.sakogbehandling;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import no.nav.vedtak.felles.integrasjon.jms.BaseJmsKonfig;

@Named("SakOgBehandling")
@ApplicationScoped
public class SakOgBehandlingJmsKonfig extends BaseJmsKonfig {

    public static final String JNDI_QUEUE = "jms/QueueSakOgBehandling";

    private static final String QUEUE_PREFIX = "SBEH_SAKSBEHANDLING";

    public SakOgBehandlingJmsKonfig() {
        super(QUEUE_PREFIX);
    }
}
