package no.nav.vedtak.felles.integrasjon.kafka;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

public interface KafkaMessageHandler<K,V> {

    void handleRecord(K key, V value);

    // Configuration
    String topic();
    String groupId(); // Keep stable (or it will read from autoOffsetReset()
    default Optional<OffsetResetStrategy> autoOffsetReset() {  // Implement if other than default (LATEST). Use NONE to discover low-volume topics
        return Optional.empty();
    }

    // Deserialization - should be configured if Avro. Provided as Supplier to handle Closeable
    Supplier<Deserializer<K>> keyDeserializer();
    Supplier<Deserializer<V>> valueDeserializer();

    // Implement KafkaStringMessageHandler for json-topics. The above are for Avro-topics
    interface KafkaStringMessageHandler extends KafkaMessageHandler<String, String> {
        default Supplier<Deserializer<String>> keyDeserializer() {
            return StringDeserializer::new;
        }

        default Supplier<Deserializer<String>> valueDeserializer() {
            return StringDeserializer::new;
        }
    }
}
