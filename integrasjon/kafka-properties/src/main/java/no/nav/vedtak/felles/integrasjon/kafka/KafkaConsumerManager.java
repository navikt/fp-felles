package no.nav.vedtak.felles.integrasjon.kafka;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.KafkaConsumer;

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
        private final AtomicInteger running = new AtomicInteger(ConsumerState.UNINITIALIZED.hashCode());

        public KafkaConsumerLoop(KafkaMessageHandler<K,V> handler) {
            this.handler = handler;
        }

        // Implementert som at-least-once - krever passe idempotente handleRecord og regner med at de er Transactional (commit hvert kall)
        // Hvis man vil komplisere ting så kan gå for exactly-once -  håndtere OffsetCommit (set property ENABLE_AUTO_COMMIT_CONFIG false)
        // Man må da være bevisst på samspill DB-commit og Offset-commit - lage en Transactional handleRecords for alle som er pollet.
        // handleRecords må ta inn ConsumerRecords (alle pollet) og 2 callbacks som a) legger til konsumert og b) kaller commitAsync(konsumert)
        // Dessuten må man catche WakeupException og andre exceptions og avstemme håndtering (OffsetCommit) med DB-TX-Commit
        @Override
        public void run() {
            try(var key = handler.keyDeserializer().get(); var value = handler.valueDeserializer().get()) {
                var props = KafkaProperties.forConsumerGenericValue(handler.groupId(), key, value, handler.autoOffsetReset().orElse(null));
                consumer = new KafkaConsumer<>(props, key, value);
                consumer.subscribe(List.of(handler.topic()));
                running.set(RUNNING);
                while (running.get() == RUNNING) {
                    var records = consumer.poll(POLL_TIMEOUT);
                    for (var record : records) {
                        handler.handleRecord(record.key(), record.value());
                    }
                }
            } finally {
                if (consumer != null) {
                    consumer.close(CLOSE_TIMEOUT);
                }
                running.set(ConsumerState.STOPPED.hashCode());
            }
        }

        public void shutdown() {
            if (running.get() == RUNNING) {
                running.set(ConsumerState.STOPPING.hashCode());
            } else {
                running.set(ConsumerState.STOPPED.hashCode());
            }
            // Kan vurdere consumer.wakeup() + håndtere WakeupException ovenfor - men har utelatt til fordel for en tilstand og polling med kort timeout
        }

        public KafkaMessageHandler<K, V> handler() {
            return handler;
        }

        public boolean isRunning() {
            return running.get() == RUNNING;
        }

        public boolean isStopped() {
            return running.get() == ConsumerState.STOPPED.hashCode();
        }
    }
}
