package no.nav.vedtak.felles.integrasjon.jms;

import javax.jms.JMSContext;
import javax.jms.JMSException;

/**
 * Baseklasse for meldingsdrevne beans for "eksterne" køer (dvs. fysisk på annen MQ server enn VL bruker).
 * </p>
 * <p>
 * (Dette krever en annen implementasjon av selftest enn for interne køer.)
 */
public abstract class ExternalQueueConsumer extends QueueConsumer {

    public ExternalQueueConsumer() {
    }

    public ExternalQueueConsumer(JmsKonfig konfig) {
        super(konfig);
    }

    public ExternalQueueConsumer(JmsKonfig konfig, int sessionMode) {
        super(konfig, sessionMode);
    }

    @Override
    public void testConnection() throws JMSException {
        if (isDisabled()) return;

        try (var context = createContext()) {
            try (var consumer = context.createConsumer(getQueue());) {
                // autocloses
            }
        }
    }
}
