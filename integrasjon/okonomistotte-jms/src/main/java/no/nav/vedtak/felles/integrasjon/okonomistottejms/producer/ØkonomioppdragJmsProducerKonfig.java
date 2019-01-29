package no.nav.vedtak.felles.integrasjon.okonomistottejms.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import no.nav.vedtak.felles.integrasjon.jms.BaseJmsKonfig;

@Named("økonomioppdragjmsproducerkonfig")
@ApplicationScoped
public class ØkonomioppdragJmsProducerKonfig extends BaseJmsKonfig {

    public static final String JNDI_QUEUE = "jms/QueueFpsakOkonomiOppdragSend";

    private static final String UT_QUEUE_PREFIX = "fpsak_okonomi_oppdrag_send";

    public ØkonomioppdragJmsProducerKonfig() {
        super(UT_QUEUE_PREFIX);
    }
}
