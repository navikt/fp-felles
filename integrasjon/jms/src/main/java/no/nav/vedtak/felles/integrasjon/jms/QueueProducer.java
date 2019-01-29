package no.nav.vedtak.felles.integrasjon.jms;

import java.util.Map;
import java.util.function.Consumer;

import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;

import no.nav.vedtak.felles.integrasjon.jms.pausing.MQExceptionUtil;

/**
 * Baseklasse for klienter som skriver meldinger til kø.
 */
public abstract class QueueProducer extends QueueBase {

    public QueueProducer() {
    }

    public QueueProducer(JmsKonfig konfig) {
        super(konfig);
    }

    public QueueProducer(JmsKonfig konfig, int sessionMode) {
        super(konfig, sessionMode);
    }

    protected JMSContext createContext() {
        return getConnectionFactory().createContext(getUsername(), getPassword(), getSessionMode());
    }

    protected void doWithContext(Consumer<JMSContext> consumer) {
        // TODO (FC) : JMSContext kan caches per tråd i en ThreadLocal for å redusere turnover av ressurser hvis det
        // blir et ytelsesproblem. Bør antagelig da lages nytt ved Exception.
        try (JMSContext context = createContext()) {
            consumer.accept(context);
        } catch (JMSRuntimeException e) {
            CharSequence mqExceptionDetails = MQExceptionUtil.extract(e);
            throw QueueProducerFeil.FACTORY.uventetFeilVedSendingAvMelding(mqExceptionDetails, e).toException();
        }
    }

    public void sendMessage(Message message) {
        doWithContext((ctx) -> doSendMessage(message, ctx));
    }

    protected void doSendMessage(Message message, JMSContext context) {
        JMSProducer producer = context.createProducer();
        producer.send(getQueue(), message);
    }

    public void sendTextMessage(JmsMessage message) {
        doWithContext((ctx) -> doSendTextMessage(message, ctx));
    }

    protected void doSendTextMessage(JmsMessage message, JMSContext context) {
        JMSProducer producer = context.createProducer();
        if (message.hasHeaders()) {
            for (Map.Entry<String, String> entry : message.getHeaders().entrySet()) {
                producer.setProperty(entry.getKey(), entry.getValue());
            }
        }
        producer.send(getQueue(), message.getText());
    }
}
