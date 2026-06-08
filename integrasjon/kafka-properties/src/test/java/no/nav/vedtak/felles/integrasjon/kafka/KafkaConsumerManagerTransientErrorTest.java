package no.nav.vedtak.felles.integrasjon.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.kafka.KafkaContainer;


class KafkaConsumerManagerTransientErrorTest {

    private static final String TOPIC = "test-topic-" + UUID.randomUUID();

    private static KafkaContainer kafka;

    private KafkaConsumerManager<String, String> manager;

    @BeforeAll
    static void startBroker() throws Exception {
        kafka = new KafkaContainer("apache/kafka:latest");
        kafka.start();

        System.setProperty("KAFKA_BROKERS", kafka.getBootstrapServers());

        createTopic(TOPIC);
    }

    @AfterAll
    static void stopBroker() {
        System.clearProperty("KAFKA_BROKERS");
        if (kafka != null) {
            kafka.stop();
        }
    }

    @AfterEach
    void stopManager() {
        if (manager != null) {
            manager.stop();
        }
    }


    @Test
    void kafkaManager_skalIkkeTapeMeldinger() throws Exception {
        produceMessages(TOPIC, "MSG-0", "MSG-1", "MSG-2", "MSG-3");

        var handler = new TransientlyFailingHandler(TOPIC);
        var errors = Collections.synchronizedList(new ArrayList<Throwable>());

        manager = new KafkaConsumerManager<>(handler);
        manager.start((topic, ex) -> errors.add(ex));

        await("Handler skal være i gang med å prosessere meldinger")
            .atMost(Duration.ofSeconds(30))
            .until(() -> handler.processed().contains("MSG-0"));

        await("Handler kaster exception og manager dør")
            .atMost(Duration.ofSeconds(50))
            .until(() -> manager.allStopped());

        produceMessages(TOPIC, "MSG-4");

        // starter en ny manager med samme handler
        manager = new KafkaConsumerManager<>(handler);
        manager.start((topic, ex) -> errors.add(ex));

        await("Handler skal ha prosessert ny melding")
            .atMost(Duration.ofSeconds(30))
            .until(() -> handler.processed().contains("MSG-4"));

        // Følgende asserts feiler med gammel KafkaConsumerManager (inneholder da ["MSG-0", "MSG-4"])
        // Årsaken er at timeout overstiger auto commit interval, slik at offset for hele gruppen commites. Uprosesserte meldinger tapes ved ny start.
        //
        // Asserter ok med ny kafka manager med manuell commit.
        assertThat(handler.processed()).containsAll(Set.of("MSG-0", "MSG-1", "MSG-2", "MSG-3", "MSG-4"));
        assertThat(handler.processed()).filteredOn(v -> !v.equalsIgnoreCase("MSG-1")).allMatch(v -> handler.attemptCount(v) == 1);
        assertThat(handler.processed()).filteredOn(v -> v.equalsIgnoreCase("MSG-1")).allMatch(v -> handler.attemptCount(v) == 2);
    }

    private static void createTopic(String topic) throws Exception {
        var props = new Properties();
        props.put("bootstrap.servers", kafka.getBootstrapServers());
        try (var admin = AdminClient.create(props)) {
            admin.createTopics(List.of(new NewTopic(topic, 1, (short) 1)))
                .all()
                .get(10, TimeUnit.SECONDS);
        }
    }

    private static void produceMessages(String topic, String... values) throws Exception {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        try (var producer = new KafkaProducer<String, String>(props)) {
            for (var value : values) {
                producer.send(new ProducerRecord<>(topic, value)).get(5, TimeUnit.SECONDS);
            }
        }
    }

    static class TransientlyFailingHandler implements KafkaMessageHandler.KafkaStringMessageHandler {

        private static final String TRIGGER_MSG = "MSG-1";

        private final String topicName;
        private static final String groupId = "test-group-" + UUID.randomUUID();

        private final Set<String> processedValues = ConcurrentHashMap.newKeySet();
        private final Map<String, AtomicInteger> attempts = new ConcurrentHashMap<>();
        private final AtomicBoolean poisonFired = new AtomicBoolean(false);

        TransientlyFailingHandler(String topicName) {
            this.topicName = topicName;
        }

        /**
         * Kaster RuntimeException for MSG-1 første gang – simulerer en exception som inntreffer etter timeout som overstiger auto commit interval
         */
        @Override
        public void handleRecord(String key, String value) {
            attempts.computeIfAbsent(value, k -> new AtomicInteger()).incrementAndGet();

            if (TRIGGER_MSG.equals(value) && !poisonFired.getAndSet(true)) {
                // Kaster uten å legge til i processed – simulerer at proxied @Transaction-annotert handler impl timer ut i påvente av db-connection
                // Kommentarer om feilsituasjon gjelder auto commit-versjonen av KafkaConsumerManager.
                try {
                    // "venter på connection" lenger enn auto commit interval for å trigge kafka commit
                    // MAX_POLL_INTERVAL_MS_CONFIG er satt med default verdi 5000ms
                    Thread.sleep(10_000);
                    // Runtime exception kaster prosessering av resten av meldingene i poll som nå er commited mot kafka
                    throw new RuntimeException("Simulert transient feil: kunne ikke hente DB-tilkobling fra pool");
                }
                catch (InterruptedException e) {
                    // satser på at denne ikke dukker opp
                }
            }
            processedValues.add(value);
        }

        @Override
        public String topic() {
            return topicName;
        }

        @Override
        public String groupId() {
            return groupId;
        }

        @Override
        public Optional<StrategyType> autoOffsetReset() {
            // EARLIEST slik at vi plukker opp meldingene produsert i @BeforeAll
            return Optional.of(StrategyType.EARLIEST);
        }

        Set<String> processed() {
            return processedValues;
        }

        int attemptCount(String value) {
            var counter = attempts.get(value);
            return counter == null ? 0 : counter.get();
        }
    }
}

