package no.nav.vedtak.felles.integrasjon.kafka;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import no.nav.vedtak.sikkerhet.kontekst.BasisKontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

import org.apache.kafka.clients.consumer.CloseOptions;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

public class KafkaConsumerManager<K, V> {

    private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(10);

    private final List<KafkaConsumerLoop<K, V>> consumers;

    public KafkaConsumerManager(KafkaMessageHandler<K, V> handler) {
        this(List.of(handler));
    }

    public KafkaConsumerManager(List<? extends KafkaMessageHandler<K, V>> handlers) {
        this.consumers = handlers.stream().map(KafkaConsumerLoop::new).toList();
    }

    public void start(BiConsumer<String, Throwable> errorLogger) {
        consumers.forEach(c -> {
            c.setErrorLogger(errorLogger);
            var ct = new Thread(c, "KC-" + c.handler().groupId());
            ct.setUncaughtExceptionHandler((_t, e) -> { errorLogger.accept(c.handler().topic(), e); stop(); });
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

    private record KafkaConsumerCloser<K, V>(List<KafkaConsumerLoop<K, V>> consumers) implements Runnable {
        @Override
        public void run() {
            consumers.forEach(KafkaConsumerLoop::shutdown);
        }
    }

    public static class KafkaConsumerLoop<K, V> implements Runnable {

        private static final Duration POLL_TIMEOUT = Duration.ofSeconds(1);
        private static final Duration COMMIT_TIMEOUT = Duration.ofSeconds(5);
        private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(10);

        private enum ConsumerState { UNINITIALIZED, RUNNING, STOPPING, STOPPED }

        private final KafkaMessageHandler<K, V> handler;
        private final AtomicReference<ConsumerState> consumerState = new AtomicReference<>(ConsumerState.UNINITIALIZED);
        private final Map<TopicPartition, OffsetAndMetadata> processedOffsets = new HashMap<>();

        private volatile KafkaConsumer<K, V> consumer;
        private BiConsumer<String, Throwable> errorLogger;

        public KafkaConsumerLoop(KafkaMessageHandler<K, V> handler) {
            this.handler = handler;
        }

        void setErrorLogger(BiConsumer<String, Throwable> errorLogger) {
            this.errorLogger = errorLogger;
        }

        @Override
        public void run() {
            try (var key = handler.keyDeserializer().get();
                 var value = handler.valueDeserializer().get()) {

                KontekstHolder.setKontekst(BasisKontekst.forProsesstaskUtenSystembruker());

                var props = KafkaProperties.forConsumerGenericValue(
                    handler.groupId(), key, value, handler.autoOffsetReset().orElse(null));

                consumer = new KafkaConsumer<>(props, key, value);

                consumer.subscribe(List.of(handler.topic()), new ConsumerRebalanceListener() {
                    @Override
                    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                        commitSyncFor(partitions);
                        partitions.forEach(processedOffsets::remove);
                    }

                    @Override
                    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                        // Kafka's committed offsets are used directly – nothing to do.
                    }

                    @Override
                    public void onPartitionsLost(Collection<TopicPartition> partitions) {
                        // Partitions already belong to another consumer – do not commit.
                        partitions.forEach(processedOffsets::remove);
                    }
                });

                consumerState.set(ConsumerState.RUNNING);

                while (consumerState.get() == ConsumerState.RUNNING) {
                    var records = consumer.poll(POLL_TIMEOUT);

                    records.forEach(record -> {
                        handler.handleRecord(record.key(), record.value());
                        processedOffsets.put(
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1));
                    });

                    if (!records.isEmpty()) {
                        commitAsync();
                    }
                }

                commitSync();

            } catch (WakeupException e) {
                if (consumerState.get() != ConsumerState.STOPPING) {
                    throw e;
                }
                commitSync();
            } finally {
                try {
                    if (consumer != null) {
                        consumer.close(CloseOptions.timeout(CLOSE_TIMEOUT));
                    }
                } finally {
                    consumerState.set(ConsumerState.STOPPED);
                    if (KontekstHolder.harKontekst()) {
                        KontekstHolder.fjernKontekst();
                    }
                }
            }
        }

        /** Non-blocking commit after a successfully processed poll batch. */
        private void commitAsync() {
            if (processedOffsets.isEmpty()) return;
            consumer.commitAsync(new HashMap<>(processedOffsets), (_offsets, ex) -> {
                if (ex != null && errorLogger != null) {
                    errorLogger.accept(handler.topic(), ex);
                }
            });
        }

        /** Blocking commit – used on clean shutdown and after a WakeupException. */
        private void commitSync() {
            if (consumer == null || processedOffsets.isEmpty()) return;
            try {
                consumer.commitSync(new HashMap<>(processedOffsets), COMMIT_TIMEOUT);
            } catch (Exception e) {
                if (errorLogger != null) errorLogger.accept(handler.topic(), e);
            }
        }

        /** Blocking commit for a specific set of partitions – used on rebalance revocation. */
        private void commitSyncFor(Collection<TopicPartition> partitions) {
            var offsets = new HashMap<TopicPartition, OffsetAndMetadata>();
            partitions.forEach(tp -> {
                var offset = processedOffsets.get(tp);
                if (offset != null) offsets.put(tp, offset);
            });
            if (offsets.isEmpty()) return;
            try {
                consumer.commitSync(offsets, COMMIT_TIMEOUT);
            } catch (Exception e) {
                if (errorLogger != null) errorLogger.accept(handler.topic(), e);
            }
        }

        public void shutdown() {
            if (consumerState.compareAndSet(ConsumerState.RUNNING, ConsumerState.STOPPING)) {
                var c = consumer;
                if (c != null) c.wakeup();
            }
        }

        public KafkaMessageHandler<K, V> handler() { return handler; }

        public boolean isRunning() { return consumerState.get() == ConsumerState.RUNNING; }

        public boolean isStopped() { return consumerState.get() == ConsumerState.STOPPED; }
    }
}
