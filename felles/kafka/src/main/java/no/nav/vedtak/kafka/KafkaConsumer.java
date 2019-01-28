package no.nav.vedtak.kafka;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KafkaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);
    private final String topic;
    private final KafkaStreams streams;

    protected KafkaConsumer() {
        // CDI
        topic = null;
        streams = null;
    }

    protected KafkaConsumer(String topic) {
        this.topic = topic;

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-" + topic);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.POLL_MS_CONFIG, "100");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler.class);

        final StreamsBuilder builder = new StreamsBuilder();

        Consumed<String, String> stringStringConsumed = Consumed.with(Topology.AutoOffsetReset.EARLIEST);
        builder.stream(topic, stringStringConsumed)
            .foreach(this::handleMessage);

        final Topology topology = builder.build();
        streams = new KafkaStreams(topology, props);
        streams.setUncaughtExceptionHandler((t, e) -> LOGGER.warn("Feil ved consumering av kafka-topic " + topic, e));

        streams.setStateListener((newState, oldState) -> {
            LOGGER.info("Stream changed state from {} to {}", oldState, newState);
            if (newState == KafkaStreams.State.ERROR) {
                // if the stream has died there is no reason to keep spinning
                log.warn("No reason to keep living, closing stream");
                streams.close();
            }
        });
    }

    protected String getTopic() {
        return topic;
    }

    protected abstract void handleMessage(String key, String payload);

    public void start() {
        streams.start();
        log.info("Starter konsumering av {}, tilstand={}", topic, getTilstand());
    }

    public void stop() {
        streams.close();
    }

    public KafkaStreams.State getTilstand() {
        return streams.state();
    }

    public boolean isAlive() {
        return streams.state().isRunning();
    }

}
