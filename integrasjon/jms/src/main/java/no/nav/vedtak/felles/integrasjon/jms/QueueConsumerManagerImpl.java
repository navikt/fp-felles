package no.nav.vedtak.felles.integrasjon.jms;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Brukes til å starte/stoppe meldingsdrevne beans.
 */
@ApplicationScoped
public class QueueConsumerManagerImpl implements QueueConsumerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueConsumerManagerImpl.class);

    private List<QueueConsumer> consumerList;
    
    private ToggleJms toggleJms = new ToggleJms();

    // Får inn (indirekte) liste over alle beans av type QueueConsumer
    @Inject
    public void initConsumers(@Any Instance<QueueConsumer> consumersInstance) { // NOSONAR Joda, kalles av CDI
        if(isDisabled()) return;
        
        consumerList = new ArrayList<>();
        for (QueueConsumer consumer : consumersInstance) {
            consumerList.add(consumer);
        }
        LOGGER.info("initConsumers la til {} consumers", consumerList.size());
    }

    public boolean isDisabled() {
        return toggleJms.isDisabled();
    }

    @Override
    public synchronized void start() {
        if(isDisabled()) return;
        
        LOGGER.debug("start ...");
        for (QueueConsumer consumer : consumerList) {
            consumer.start();
        }
        LOGGER.info("startet");
    }

    @Override
    public synchronized void stop() {
        if(isDisabled()) return;
        
        LOGGER.debug("stop ...");
        for (QueueConsumer consumer : consumerList) {
            consumer.stop();
        }
        LOGGER.info("stoppet");
    }
}
