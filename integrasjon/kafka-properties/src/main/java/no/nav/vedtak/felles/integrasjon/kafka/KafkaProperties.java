package no.nav.vedtak.felles.integrasjon.kafka;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.apache.kafka.streams.state.internals.BlockBasedTableConfigWithAccessibleCache;
import org.rocksdb.BloomFilter;
import org.rocksdb.LRUCache;
import org.rocksdb.Options;

import no.nav.foreldrepenger.konfig.Environment;

public class KafkaProperties {

    private static final Environment ENV = Environment.current();
    private static final boolean IS_DEPLOYMENT = ENV.isProd() || ENV.isDev();
    private static final String APPLICATION_NAME = ENV.getNaisAppName();

    private KafkaProperties() {
    }

    // Alle som produserer Json-meldinger
    public static Properties forProducer() {

        final Properties props = new Properties();
        props.put(CommonClientConfigs.CLIENT_ID_CONFIG, generateClientId());
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getAivenConfig(AivenProperty.KAFKA_BROKERS));

        putSecurity(props);
        if (IS_DEPLOYMENT) {
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, getAivenConfig(AivenProperty.KAFKA_CREDSTORE_PASSWORD)); // Kun producer
        }

        // Serde
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return props;
    }

    // Alle som konsumerer Json-meldinger
    public static Properties forConsumerStringValue(String groupId) {
        return forConsumerGenericValue(groupId, new StringDeserializer(), new StringDeserializer(), Optional.empty());
    }

    public static <K,V> Properties forConsumerGenericValue(String groupId, Deserializer<K> valueKey, Deserializer<V> valueSerde, Optional<OffsetResetStrategy> offsetReset) {
        final Properties props = new Properties();

        props.put(CommonClientConfigs.GROUP_ID_CONFIG, groupId);
        props.put(CommonClientConfigs.CLIENT_ID_CONFIG, generateClientId());
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getAivenConfig(AivenProperty.KAFKA_BROKERS));
        offsetReset.ifPresent(or -> props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, or.toString()));

        putSecurity(props);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, valueKey.getClass());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueSerde.getClass());

        // Polling
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100"); // Unngå store Tx dersom alle prosesseres innen samme Tx. Default 500
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "100000"); // Gir inntil 1s pr record. Default er 600 ms/record

        return props;
    }

    // Alle som konsumerer Json-meldinger
    public static Properties forStreamsStringValue(String applicationId) {
        return forStreamsGenericValue(applicationId, Serdes.String());
    }

    public static <T> Properties forStreamsGenericValue(String applicationId, Serde<T> valueSerde) {
        final Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, generateClientId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, getAivenConfig(AivenProperty.KAFKA_BROKERS));

        putSecurity(props);

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, valueSerde.getClass());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler.class);

        props.put(StreamsConfig.DEFAULT_DSL_STORE_CONFIG, StreamsConfig.IN_MEMORY);
        props.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, StreamsRocksReadOnly.class);

        // Polling
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "200");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "60000");

        return props;
    }

    // Trengs kun for de som skal konsumere Avro. Ellers ikke
    public static String getAvroSchemaRegistryURL() {
        return getAivenConfig(AivenProperty.KAFKA_SCHEMA_REGISTRY);
    }

    // Trengs kun for de som skal konsumere Avro. Ellers ikke
    public static String getAvroSchemaRegistryBasicAuth() {
        return getAivenConfig(AivenProperty.KAFKA_SCHEMA_REGISTRY_USER) + ":" + getAivenConfig(AivenProperty.KAFKA_SCHEMA_REGISTRY_PASSWORD);
    }


    private static String getAivenConfig(AivenProperty property) {
        return Optional.ofNullable(ENV.getProperty(property.name()))
            .orElseGet(() -> ENV.getProperty(property.name().toLowerCase().replace('_', '.')));
    }

    private static String generateClientId() {
        return APPLICATION_NAME + "-" + UUID.randomUUID();
    }

    private static void putSecurity(Properties props) {
        if (IS_DEPLOYMENT) {
            var credStorePassword = getAivenConfig(AivenProperty.KAFKA_CREDSTORE_PASSWORD);
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks");
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, getAivenConfig(AivenProperty.KAFKA_TRUSTSTORE_PATH));
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, credStorePassword);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, getAivenConfig(AivenProperty.KAFKA_KEYSTORE_PATH));
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, credStorePassword);
        } else {
            props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
            props.setProperty(SaslConfigs.SASL_MECHANISM, "PLAIN");
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, "vtp", "vtp");
            props.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasCfg);
        }
    }

    public static class StreamsRocksReadOnly implements RocksDBConfigSetter {

        @Override
        public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {

            BlockBasedTableConfigWithAccessibleCache tableConfig = new BlockBasedTableConfigWithAccessibleCache();
            tableConfig.setBlockCache(new LRUCache(1024 * 1024L));
            tableConfig.setBlockSize(4096L);
            tableConfig.setFilterPolicy(new BloomFilter());
            tableConfig.setCacheIndexAndFilterBlocks(true);
            options.setTableFormatConfig(tableConfig);

            options.setWriteBufferSize(512 * 1024L);
            options.setMaxWriteBufferNumber(2);
        }

        @Override
        public void close(final String storeName, final Options options) {
            // NOOP
        }
    }

}
