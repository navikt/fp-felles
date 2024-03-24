package no.nav.vedtak.felles.integrasjon.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import no.nav.vedtak.exception.IntegrasjonException;

public class KafkaSender {

    private final Producer<String, String> producer;
    private final String topic;

    public KafkaSender(String topic) {
        this.producer = new KafkaProducer<>(KafkaProperties.forProducer());
        this.topic = topic;
    }

    public RecordMetadata send(String key, String message) {
        if (topic == null) {
            throw kafkaPubliseringException("null", new IllegalArgumentException());
        }
        return send(key, message, this.topic);
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
