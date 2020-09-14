package no.nav.vedtak.felles.integrasjon.jms;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QueueConsumerManagerImplTest {

    private static final class TestQueueConsumerManagerImpl extends QueueConsumerManagerImpl {
        @Override
        public boolean isDisabled() {
            return false;
        }
    }

    private QueueConsumerManagerImpl manager; // the object we're testing

    @BeforeEach
    public void setup() {
        manager = new TestQueueConsumerManagerImpl();
    }

    @Test
    public void test_initStartStop() {

        QueueConsumer mockConsumer1 = mock(QueueConsumer.class);
        QueueConsumer mockConsumer2 = mock(QueueConsumer.class);
        QueueConsumer mockConsumer3 = mock(QueueConsumer.class);
        List<QueueConsumer> mockConsumersList = Arrays.asList(mockConsumer1, mockConsumer2, mockConsumer3);
        @SuppressWarnings("unchecked")
        Instance<QueueConsumer> mockConsumersInstance = mock(Instance.class);
        when(mockConsumersInstance.iterator()).thenReturn(mockConsumersList.iterator());

        manager.initConsumers(mockConsumersInstance);
        manager.start();

        verify(mockConsumer1).start();
        verify(mockConsumer2).start();
        verify(mockConsumer3).start();

        manager.stop();

        verify(mockConsumer1).stop();
        verify(mockConsumer2).stop();
        verify(mockConsumer3).stop();
    }
}
