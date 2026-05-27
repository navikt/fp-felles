package no.nav.vedtak.felles.integrasjon.kafka;

import java.util.Optional;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeader;

import no.nav.vedtak.exception.IntegrasjonException;

public class KafkaSender {

    private final Producer<String, String> producer;
    private final String topicName;

    public KafkaSender(String topicName) {
        this.producer = new KafkaProducer<>(KafkaProperties.forProducer());
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }

    public record KafkaHeader(String key, String value) {}

    public RecordMetadata send(String key, String message) {
        if (topicName == null) {
            throw kafkaPubliseringException("null", new IllegalArgumentException());
        }
        return send(null, key, message, topicName);
    }

    public RecordMetadata send(KafkaHeader header, String key, String message) {
        if (topicName == null) {
            throw kafkaPubliseringException("null", new IllegalArgumentException());
        }
        return send(header, key, message, this.topicName);
    }

    public RecordMetadata send(String key, String message, String topic) {
        return send(null, key, message, topic);
    }

    public RecordMetadata send(KafkaHeader header, String key, String message, String topic) {
        try {
            var kafkaRecord = new ProducerRecord<>(topic, key, message);
            Optional.ofNullable(header).ifPresent(h -> kafkaRecord.headers().add(new RecordHeader(h.key(), h.value().getBytes())));
            return producer.send(kafkaRecord).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw kafkaPubliseringException(topic, e);
        } catch (Exception e) {
            throw kafkaPubliseringException(topic, e);
        }
    }

    private IntegrasjonException kafkaPubliseringException(String topic, Exception e) {
        return new IntegrasjonException("F-KAFKA-925475", "Unexpected error when sending message to topic " + topic, e);
    }

}
