package no.nav.vedtak.felles.integrasjon.okonomistottejms.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.finn.unleash.Unleash;
import no.nav.vedtak.felles.integrasjon.jms.BaseJmsKonfig;


/**
* @deprecated Flytt til fpoppdrag
*/
@Deprecated
@Named("økonomioppdragjmsproducerkonfig")
@ApplicationScoped
public class ØkonomioppdragJmsProducerKonfig extends BaseJmsKonfig {

    public static final String JNDI_QUEUE = "jms/QueueFpsakOkonomiOppdragSend";

    private static final String UT_QUEUE_PREFIX = "fpsak_okonomi_oppdrag_send";
    private static final String KVITTERING_QUEUE_PREFIX = "fpsak_okonomi_oppdrag_mottak";

    @Inject
    public ØkonomioppdragJmsProducerKonfig(Unleash unleash) {
        super(UT_QUEUE_PREFIX, unleash.isEnabled("fpsak.send.kvitteringskoenavn") ? KVITTERING_QUEUE_PREFIX : null);
    }
}

