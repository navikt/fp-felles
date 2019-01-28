package no.nav.vedtak.kafka;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.apptjeneste.AppServiceHandler;

@ApplicationScoped
public class KafkaConsumerManager implements AppServiceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerManager.class);

    private List<KafkaConsumer> consumerList;

    KafkaConsumerManager() {
    }

    @Inject
    public KafkaConsumerManager(@Any Instance<KafkaConsumer> consumersInstance) {
        consumerList = new ArrayList<>();
        for (KafkaConsumer consumer : consumersInstance) {
            consumerList.add(consumer);
        }
        LOGGER.info("La til {} consumers", consumerList.size());
    }

    @Override
    public void start() {
        LOGGER.debug("Starter consumere ...");
        for (KafkaConsumer consumer : consumerList) {
            LOGGER.info("Starter consumer av topic {}", consumer.getTopic());
            consumer.start();
        }
        LOGGER.info("Startet");
    }

    @Override
    public void stop() {
        LOGGER.debug("Stopper consumere ...");
        for (KafkaConsumer consumer : consumerList) {
            LOGGER.info("Stopper consumer av topic {}", consumer.getTopic());
            consumer.stop();
        }
        LOGGER.info("Stoppet");
    }
}
