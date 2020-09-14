package no.nav.vedtak.felles.integrasjon.jms;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Enumeration;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.TextMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("resource")
public class InternalSelftestQueueProducerTest {

    private InternalQueueProducer helper; // the object we're testing

    private JMSContext mockContext;
    private Queue mockQueue;
    private JMSConsumer mockConsumer;
    private JMSProducer mockProducer;
    private QueueBrowser mockBrowser;
    private TextMessage mockMessage;

    private static final String MSG_TEXT = "beskjeden";

    @BeforeEach
    public void setup() throws JMSException {

        mockContext = mock(JMSContext.class);
        mockQueue = mock(Queue.class);
        mockConsumer = mock(JMSConsumer.class);
        mockProducer = mock(JMSProducer.class);
        mockMessage = mock(TextMessage.class);
        mockBrowser = mock(QueueBrowser.class);
        BaseJmsKonfig jmsKonfig = mock(BaseJmsKonfig.class);
        helper = new TestInternalQueueProducer(jmsKonfig) {
            @Override
            protected JMSContext createContext() {
                return mockContext;
            }
        };
        when(mockContext.createConsumer(mockQueue)).thenReturn(mockConsumer);
        when(mockContext.createProducer()).thenReturn(mockProducer);
        when(mockContext.createBrowser(mockQueue)).thenReturn(mockBrowser);

        when(mockMessage.getText()).thenReturn(MSG_TEXT);

        Whitebox.setInternalState(helper, "queue", mockQueue);
    }

    @Test
    public void test_sendMessage() {

        helper.sendMessage(mockMessage);

        verify(mockProducer).send(mockQueue, mockMessage);
    }

    @Test
    public void test_sendTextMessage() {
        final JmsMessage build = JmsMessage.builder().withMessage(MSG_TEXT).build();
        helper.sendTextMessage(build);

        verify(mockProducer).send(mockQueue, MSG_TEXT);
    }

    @Test
    public void test_testConnection_queueIsOnAppsQueueMgr() throws JMSException {

        @SuppressWarnings("rawtypes")
        Enumeration mockMsgsEnumeration = mock(Enumeration.class);
        when(mockMsgsEnumeration.hasMoreElements()).thenReturn(true);
        when(mockMsgsEnumeration.nextElement()).thenReturn(mockMessage);
        when(mockBrowser.getEnumeration()).thenReturn(mockMsgsEnumeration);

        helper.testConnection();

        verify(mockBrowser).getEnumeration();
        verify(mockMsgsEnumeration).hasMoreElements();
        verify(mockMsgsEnumeration).nextElement();
        verify(mockBrowser).close();
    }

    class TestInternalQueueProducer extends InternalQueueProducer {

        TestInternalQueueProducer(JmsKonfig konfig) {
            super(konfig);
        }

        @Override
        public boolean isDisabled() {
            return false;
        }
    }
}
