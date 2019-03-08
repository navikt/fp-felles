package no.vedtak.felles.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class MeldingConsumerImpl implements MeldingConsumer {

    private static final int TIMEOUT = 10000;
    private KafkaConsumer<String, String> kafkaConsumer;
    private String topic;

    @Inject
    public MeldingConsumerImpl(@KonfigVerdi("kafka.aksjonspunkthendelse.topic") String topic,
                               @KonfigVerdi("bootstrap.servers") String bootstrapServers,
                               @KonfigVerdi("kafka.aksjonspunkthendelse.schema.registry.url") String schemaRegistryUrl,
                               @KonfigVerdi("kafka.aksjonspunkthendelse.group.id") String groupId,
                               @KonfigVerdi("systembruker.username") String username,
                               @KonfigVerdi("systembruker.password") String password) {

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

    private void setSecurity(String username, Properties properties) {
        if (username != null && !username.isEmpty()) {
            properties.put("security.protocol", "SASL_SSL");
            properties.put("sasl.mechanism", "PLAIN");
        }
    }


    private void addUserToProperties(@KonfigVerdi("kafka.username") String username, @KonfigVerdi("kafka.password") String password, Properties properties) {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, username, password);
            properties.put("sasl.jaas.config", jaasCfg);
        }
    }

    @Override
    public List<String> hentConsumerMeldingene() {
        ConsumerRecords<String, String> records = kafkaConsumer.poll(TIMEOUT);
        List<String> responseStringList = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records) {
            responseStringList.add(record.value());
        }
        return responseStringList;
    }

    private void subscribe() {
        kafkaConsumer.subscribe(Collections.singletonList(topic));
    }

    @Override
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

    @Override
    public void close() {
        kafkaConsumer.close();
    }

    @Override
    public void manualCommitSync() {
        kafkaConsumer.commitSync();
    }

    @Override
    public void manualCommitAsync() {
        kafkaConsumer.commitAsync();
    }

    private KafkaConsumer<String, String> createConsumer(Properties properties) {
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put("auto.offset.reset", "earliest");
        return new KafkaConsumer<>(properties);
    }

}
