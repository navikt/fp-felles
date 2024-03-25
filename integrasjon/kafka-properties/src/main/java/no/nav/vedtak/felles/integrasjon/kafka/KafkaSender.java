package no.nav.vedtak.felles.integrasjon.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

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

    public RecordMetadata send(String key, String message) {
        if (topicName == null) {
            throw kafkaPubliseringException("null", new IllegalArgumentException());
        }
        return send(key, message, this.topicName);
    }

    public RecordMetadata send(String key, String message, String topic) {
        try {
            var record = new ProducerRecord<>(topic, key, message);
            return producer.send(record).get();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw kafkaPubliseringException(topic, e);
        }
    }

    private IntegrasjonException kafkaPubliseringException(String topic, Exception e) {
        return new IntegrasjonException("F-KAFKA-925475", "Unexpected error when sending message to topic " + topic, e);
    }

}
