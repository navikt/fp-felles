package no.nav.vedtak.felles.integrasjon.jms.sessionmode;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;

import javax.jms.JMSContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientAckSessionModeStrategyTest {

    private ClientAckSessionModeStrategy strategy; // the object we're testing

    @Mock
    private JMSContext mockJMSContext;

    @BeforeEach
    public void setup() {
        strategy = new ClientAckSessionModeStrategy();
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
