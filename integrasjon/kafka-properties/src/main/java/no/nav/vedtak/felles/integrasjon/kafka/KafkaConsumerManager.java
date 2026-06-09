package no.nav.vedtak.felles.integrasjon.kafka;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import no.nav.vedtak.sikkerhet.kontekst.BasisKontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

import org.apache.kafka.clients.consumer.CloseOptions;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaConsumerManager<K, V> {

    private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(10);

    private final List<KafkaConsumerLoop<K, V>> consumers;

    public KafkaConsumerManager(KafkaMessageHandler<K, V> handler) {
        this(List.of(handler));
    }

    public KafkaConsumerManager(List<? extends KafkaMessageHandler<K, V>> handlers) {
        this.consumers = handlers.stream().map(KafkaConsumerLoop::new).toList();
    }

    public void start(BiConsumer<String, Throwable> errorlogger) {
        consumers.forEach(c -> {
            c.setErrorLogger(errorlogger);
            var ct = new Thread(c, "KC-" + c.handler().groupId());
            ct.setUncaughtExceptionHandler((_t, e) -> {
                errorlogger.accept(c.handler().topic(), e);
                stop();
            });
            ct.start();
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> consumers.forEach(KafkaConsumerLoop::shutdown)));
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

    public static class KafkaConsumerLoop<K, V> implements Runnable {
        private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);
        private enum ConsumerState { UNINITIALIZED, RUNNING, STOPPING, STOPPED;}

        private final KafkaMessageHandler<K, V> handler;
        private final AtomicReference<ConsumerState> consumerState = new AtomicReference<>(ConsumerState.UNINITIALIZED);

        private BiConsumer<String, Throwable> errorLogger;
        private KafkaConsumer<K, V> consumer;

        public KafkaConsumerLoop(KafkaMessageHandler<K, V> handler) {
            this.handler = handler;
        }

        public void setErrorLogger(BiConsumer<String, Throwable> errorLogger) {
            this.errorLogger = errorLogger;
        }


        // Implementert som at-least-once - krever passe idempotente handleRecord og regner med at de er Transactional (commit hvert kall)
        // Offset vil commites mot Kafka async etter prosessering i hver loop.
        @Override
        public void run() {
            try (var key = handler.keyDeserializer().get(); var value = handler.valueDeserializer().get()) {
                KontekstHolder.setKontekst(BasisKontekst.forProsesstaskUtenSystembruker());
                var props = KafkaProperties.forConsumerGenericValue(handler.groupId(), key, value, handler.autoOffsetReset().orElse(null));
                consumer = new KafkaConsumer<>(props, key, value);
                consumer.subscribe(List.of(handler.topic()));
                consumerState.set(ConsumerState.RUNNING);

                while (consumerState.get() == ConsumerState.RUNNING) {
                    var krecords = consumer.poll(POLL_TIMEOUT);
                    for (var krecord : krecords) {
                        handler.handleRecord(krecord.key(), krecord.value());
                    }
                    if (!krecords.isEmpty()) {
                        consumer.commitAsync((_offsets, ex) -> {
                            if (ex != null && errorLogger != null) {
                                errorLogger.accept(handler.topic(), ex);
                            }
                        });
                    }
                }
            } finally {
                if (consumer != null) {
                    consumer.close(CloseOptions.timeout(CLOSE_TIMEOUT));
                }
                consumerState.set(ConsumerState.STOPPED);
                if (KontekstHolder.harKontekst()) {
                    KontekstHolder.fjernKontekst();
                }
            }
        }

        public void shutdown() {
            if (consumerState.get() == ConsumerState.RUNNING) {
                consumerState.compareAndSet(ConsumerState.RUNNING, ConsumerState.STOPPING);
            } else {
                consumerState.set(ConsumerState.STOPPED);
            }
        }

        public KafkaMessageHandler<K, V> handler() { return handler; }

        public boolean isRunning() { return consumerState.get() == ConsumerState.RUNNING; }

        public boolean isStopped() { return consumerState.get() == ConsumerState.STOPPED; }
    }
}
