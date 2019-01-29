package no.nav.vedtak.felles.integrasjon.jms.sessionmode;

import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.Queue;

/**
 * @deprecated TODO (rune) Denne klassen er en "last resort" hvis vi ikke får client-ack til å virke på JBoss.
 */
@Deprecated
class AutoAckSessionModeStrategy implements SessionModeStrategy {

    @Override
    public int getSessionMode() {
        return JMSContext.AUTO_ACKNOWLEDGE;
    }

    @Override
    public void commitReceivedMessage(JMSContext context) {
        // Ikke noe. "commit" gjøres automatisk i det melding leses, pga. JMSContext.AUTO_ACKNOWLEDGE.
    }

    @Override
    public void rollbackReceivedMessage(JMSContext context, Queue queue, Message message) {
        // "rollback" er manuell re-posting av meldingen:
        context.createProducer().send(queue, message);
    }
}
