package no.nav.vedtak.felles.integrasjon.jms.sessionmode;

import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.Queue;

public class ClientAckSessionModeStrategy implements SessionModeStrategy {

    @Override
    public int getSessionMode() {
        return JMSContext.CLIENT_ACKNOWLEDGE;
    }

    @Override
    public void commitReceivedMessage(JMSContext context) {
        context.acknowledge();
    }

    @Override
    public void rollbackReceivedMessage(JMSContext context, Queue queue, Message message) {
        context.recover();
    }
}
