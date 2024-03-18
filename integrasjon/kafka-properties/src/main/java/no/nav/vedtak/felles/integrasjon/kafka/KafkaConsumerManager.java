package no.nav.vedtak.felles.integrasjon.kafka;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

public class KafkaConsumerManager<K,V> {

    private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(10);

    private final List<KafkaMessageHandler<K,V>> handlers;
    private final List<KafkaConsumerLoop<K,V>> consumers = new ArrayList<>();

    public KafkaConsumerManager(List<KafkaMessageHandler<K, V>> handlers) {
        this.handlers = handlers;
    }

    public void start(BiConsumer<String, Throwable> errorlogger) {
        consumers.addAll(handlers.stream().map(KafkaConsumerLoop::new).toList());
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
        return handlers.stream().map(KafkaMessageHandler::topic).collect(Collectors.joining(","));
    }

    private record KafkaConsumerCloser<K,V>(List<KafkaConsumerLoop<K,V>> consumers) implements Runnable {
        @Override
        public void run() {
            consumers.forEach(KafkaConsumerLoop::shutdown);
        }
    }

    public static class KafkaConsumerLoop<K,V> implements Runnable {

        private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);
        private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(10);
        private enum ConsumerState { UNINITIALIZED, RUNNING, STOPPING, STOPPED }
        private static final int RUNNING = ConsumerState.RUNNING.hashCode();

        private final KafkaMessageHandler<K, V> handler;
        private KafkaConsumer<K, V> consumer;
        private final AtomicInteger running = new AtomicInteger(ConsumerState.UNINITIALIZED.hashCode());

        public KafkaConsumerLoop(KafkaMessageHandler<K,V> handler) {
            this.handler = handler;
        }
        @Override
        public void run() {
            try(var key = handler.keyDeserializer().get(); var value = handler.valueDeserializer().get()) {
                var props = KafkaProperties.forConsumerGenericValue(handler.groupId(), key, value, handler.autoOffsetReset());
                consumer = new KafkaConsumer<>(props, key, value);
                consumer.subscribe(List.of(handler.topic()));
                running.set(RUNNING);
                while (running.get() == RUNNING) {
                    var records = consumer.poll(POLL_TIMEOUT);
                    // Hvis man vil komplisere ting så kan man håndtere både OffsetCommit og DBcommit i en Transcational handleRecords.
                    // handleRecords må ta inn alle som er pollet (records) og 2 callbacks som a) legger til konsumert og b) commitAsync(konsumert)
                    for (var record : records) {
                        handler.handleRecord(record.key(), record.value());
                    }
                }
            } catch (WakeupException e) {
                // ignore for shutdown
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
            if (consumer != null) {
                consumer.wakeup();
            }
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
