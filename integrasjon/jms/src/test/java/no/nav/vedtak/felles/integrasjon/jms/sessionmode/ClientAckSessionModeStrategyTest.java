package no.nav.vedtak.felles.integrasjon.jms.sessionmode;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.jms.JMSContext;

import org.junit.Before;
import org.junit.Test;

public class ClientAckSessionModeStrategyTest {

    private ClientAckSessionModeStrategy strategy; // the object we're testing

    private JMSContext mockJMSContext;

    @Before
    public void setup() {
        strategy = new ClientAckSessionModeStrategy();
        mockJMSContext = mock(JMSContext.class);
    }

    @Test
    public void test_getSessionMode() {
        int sessionMode = strategy.getSessionMode();
        assertThat(sessionMode).isEqualTo(JMSContext.CLIENT_ACKNOWLEDGE);
    }

    @Test
    public void test_commitReceivedMessage() {
        strategy.commitReceivedMessage(mockJMSContext);

        verify(mockJMSContext).acknowledge();
    }

    @Test
    public void test_rollbackReceivedMessage() {
        strategy.rollbackReceivedMessage(mockJMSContext, null, null);

        verify(mockJMSContext).recover();
    }
}
