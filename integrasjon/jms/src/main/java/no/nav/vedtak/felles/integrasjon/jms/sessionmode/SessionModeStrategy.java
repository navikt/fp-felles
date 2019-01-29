package no.nav.vedtak.felles.integrasjon.jms.sessionmode;

import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.Queue;

/**
 * Strategi for commit / rollback av meldinger man lykkes / ikke lykkes å prosessere.
 */
public interface SessionModeStrategy {

    int getSessionMode();

    void commitReceivedMessage(JMSContext context);

    void rollbackReceivedMessage(JMSContext context, Queue queue, Message message);
}
