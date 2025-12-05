package no.nav.vedtak.felles.integrasjon.kafka;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import no.nav.vedtak.sikkerhet.kontekst.BasisKontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

public class KafkaConsumerManager<K,V> {

    private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(10);

    private final List<KafkaConsumerLoop<K,V>> consumers;


    public KafkaConsumerManager(KafkaMessageHandler<K, V> handler) {
        this(List.of(handler));
    }

    public KafkaConsumerManager(List<? extends KafkaMessageHandler<K, V>> handlers) {
        this.consumers = handlers.stream().map(KafkaConsumerLoop::new).toList();
    }

    public void start(BiConsumer<String, Throwable> errorlogger) {
        consumers.forEach(c -> {
            var ct = new Thread(c, "KC-" + c.handler().groupId());
            ct.setUncaughtExceptionHandler((t, e) -> { errorlogger.accept(c.handler().topic(), e); stop(); });
            ct.start();
        });
        Runtime.getRuntime().addShutdownHook(new Thread(new KafkaConsumerCloser<>(consumers)));
    }

    public void stop() {
        consumers.forEach(KafkaConsumerLoop::shutdown);
        var timeout = LocalDateTime.now().plus(CLOSE_TIMEOUT).plusSeconds(1);
        while (!allStopped() && LocalDateTime.now().isBefore(timeout)) {
            try {
                Thread.sleep(Duration.ofSeconds(1));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean allRunning() {
        return consumers.stream().allMatch(KafkaConsumerLoop::isRunning);
    }

    public boolean allStopped() {
        return consumers.stream().allMatch(KafkaConsumerLoop::isStopped);
    }

    public String topicNames() {
        return consumers.stream().map(KafkaConsumerLoop::handler).map(KafkaMessageHandler::topic).collect(Collectors.joining(","));
    }

    private record KafkaConsumerCloser<K,V>(List<KafkaConsumerLoop<K,V>> consumers) implements Runnable {
        @Override
        public void run() {
            consumers.forEach(KafkaConsumerLoop::shutdown);
        }
    }

    public static class KafkaConsumerLoop<K,V> implements Runnable {

        private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);

        private enum ConsumerState { UNINITIALIZED, RUNNING, STOPPING, STOPPED }
        private static final int RUNNING = ConsumerState.RUNNING.hashCode();

        private final KafkaMessageHandler<K, V> handler;
        private KafkaConsumer<K, V> consumer;
        private final AtomicInteger consumerState = new AtomicInteger(ConsumerState.UNINITIALIZED.hashCode());

        public KafkaConsumerLoop(KafkaMessageHandler<K,V> handler) {
            this.handler = handler;
        }

        // Implementert som at-least-once - krever passe idempotente handleRecord og regner med at de er Transactional (commit hvert kall)
        // Consumeren håndterer OffsetCommit (property ENABLE_AUTO_COMMIT_CONFIG false) ved å kalle commitAsync etter hver ferdigprosesserte poll.
        // Ved rebalansering kalles commitSync med prosesserte offsets.
        @Override
        public void run() {
            try(var key = handler.keyDeserializer().get(); var value = handler.valueDeserializer().get()) {
                KontekstHolder.setKontekst(BasisKontekst.forProsesstaskUtenSystembruker());
                var props = KafkaProperties.forConsumerGenericValue(handler.groupId(), key, value, handler.autoOffsetReset().orElse(null));
                var processedOffsets = new HashMap<TopicPartition, OffsetAndMetadata>();
                consumer = new KafkaConsumer<>(props, key, value);
                consumer.subscribe(List.of(handler.topic()), new ConsumerRebalanceListener() {
                    @Override
                    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                        consumer.commitSync(processedOffsets);
                    }

                    @Override
                    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                        // n/a
                    }
                });
                consumerState.set(RUNNING);
                while (consumerState.get() == RUNNING) {
                    var krecords = consumer.poll(POLL_TIMEOUT);
                    for (var krecord : krecords) {
                        handler.handleRecord(krecord.key(), krecord.value());
                        processedOffsets.put(
                            new TopicPartition(krecord.topic(), krecord.partition()),
                            new OffsetAndMetadata(krecord.offset() + 1));
                    }
                    consumer.commitAsync(processedOffsets, null);
                }
            } finally {
                if (consumer != null) {
                    consumer.close(CLOSE_TIMEOUT);
                }
                consumerState.set(ConsumerState.STOPPED.hashCode());
                if (KontekstHolder.harKontekst()) {
                    KontekstHolder.fjernKontekst();
                }
            }
        }

        public void shutdown() {
            if (consumerState.get() == RUNNING) {
                consumerState.set(ConsumerState.STOPPING.hashCode());
            } else {
                consumerState.set(ConsumerState.STOPPED.hashCode());
            }
            // Kan vurdere consumer.wakeup() + håndtere WakeupException ovenfor - men har utelatt til fordel for en tilstand og polling med kort timeout
        }

        public KafkaMessageHandler<K, V> handler() {
            return handler;
        }

        public boolean isRunning() {
            return consumerState.get() == RUNNING;
        }

        public boolean isStopped() {
            return consumerState.get() == ConsumerState.STOPPED.hashCode();
        }
    }
}
