package no.nav.vedtak.felles.integrasjon.jms;

import javax.jms.JMSException;

/**
 * Baseklasse for sending av meldinger til "eksterne" køer (dvs. fysisk på annen MQ server enn VL bruker).</p>
 * <p>
 * (Dette krever en annen implementasjon av selftest enn for interne køer.)
 */
public abstract class ExternalQueueProducer extends QueueProducer {

    public ExternalQueueProducer() {
    }

    public ExternalQueueProducer(JmsKonfig konfig) {
        super(konfig);
    }

    public ExternalQueueProducer(JmsKonfig konfig, int sessionMode) {
        super(konfig, sessionMode);
    }

    @Override
    public void testConnection() throws JMSException {
        //NOOP ikke selftest for eksterne køer
    }
}
