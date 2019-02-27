package no.vedtak.felles.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import no.nav.vedtak.konfig.KonfigVerdi;

public class GenerellMeldingConsumer {

    private static final int TIMEOUT = 10000;
    KafkaConsumer<String, String> kafkaConsumer;
    String topic;

    public GenerellMeldingConsumer(String topic,
                                   String bootstrapServers,
                                   String schemaRegistryUrl,
                                   String groupId,
                                   String username,
                                   String password) {

        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrapServers);
        properties.put("schema.registry.url", schemaRegistryUrl);
        properties.put("group.id", groupId);
        properties.put("enable.auto.commit", "false");
        properties.put("max.poll.records", "1");

        setSecurity(username, properties);
        addUserToProperties(username, password, properties);

        this.kafkaConsumer = createConsumer(properties);
        this.topic = topic;
        subscribe();
    }

    public GenerellMeldingConsumer() {
    }

    void setSecurity(String username, Properties properties) {
        if (username != null && !username.isEmpty()) {
            properties.put("security.protocol", "SASL_SSL");
            properties.put("sasl.mechanism", "PLAIN");
        }
    }


    void addUserToProperties(@KonfigVerdi("kafka.username") String username, @KonfigVerdi("kafka.password") String password, Properties properties) {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, username, password);
            properties.put("sasl.jaas.config", jaasCfg);
        }
    }

    public List<String> hentConsumerMeldingene() {
        ConsumerRecords<String, String> records = kafkaConsumer.poll(TIMEOUT);
        List<String> responseStringList = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records) {
            responseStringList.add(record.value());
        }
        return responseStringList;
    }

    void subscribe() {
        kafkaConsumer.subscribe(Collections.singletonList(topic));
    }

    public List<String> hentConsumerMeldingeneFraStarten() {
        kafkaConsumer.poll(TIMEOUT);
        kafkaConsumer.seekToBeginning(kafkaConsumer.assignment());
        ConsumerRecords<String, String> records = kafkaConsumer.poll(1000);
        List<String> responseStringList = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records) {
            responseStringList.add(record.value());
        }
        return responseStringList;
    }

    public void close() {
        kafkaConsumer.close();
    }

    public void manualCommitSync() {
        kafkaConsumer.commitSync();
    }

    public void manualCommitAsync() {
        kafkaConsumer.commitAsync();
    }

    KafkaConsumer<String, String> createConsumer(Properties properties) {
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put("auto.offset.reset", "earliest");
        return new KafkaConsumer<>(properties);
    }

}
