package no.nav.vedtak.felles.integrasjon.jms;

import java.util.Enumeration;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.QueueBrowser;

/**
 * Baseklasse for sending av meldinger til "interne" køer (dvs. fysisk på samme MQ server som VL bruker).
 */
public abstract class InternalQueueProducer extends QueueProducer {

    public InternalQueueProducer() {
    }

    public InternalQueueProducer(JmsKonfig konfig) {
        super(konfig);
    }

    public InternalQueueProducer(JmsKonfig konfig, int sessionMode) {
        super(konfig, sessionMode);
    }

    @Override
    public void testConnection() throws JMSException {
        try (JMSContext context = createContext()) {
            try (QueueBrowser browser = context.createBrowser(getQueue())) {
                // Se på max. 1 melding, uten å konsumere den:
                Enumeration<?> msgsEnumeration = browser.getEnumeration();
                if (msgsEnumeration.hasMoreElements()) {
                    msgsEnumeration.nextElement();
                }
            }
        }
    }
}
