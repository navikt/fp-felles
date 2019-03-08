package no.nav.vedtak.felles.integrasjon.jms;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Brukes til å starte/stoppe meldingsdrevne beans.
 */
@ApplicationScoped
@Alternative
@Priority(1)
public class QueueConsumerManagerMock implements QueueConsumerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueConsumerManagerMock.class);

    // Får inn (indirekte) liste over alle beans av type QueueConsumer
    @Inject
    public void initConsumers(@SuppressWarnings("unused") @Any Instance<QueueConsumer> consumersInstance) { // NOSONAR Joda, kalles av CDI
        LOGGER.info("invoked: initConsumers - Starter ikke opp MQ consumer");
    }

    @Override
    public synchronized void start() {
        LOGGER.info("invoked: start");
    }

    @Override
    public synchronized void stop() {
        LOGGER.info("invoked: stop");
    }
}

