package no.nav.vedtak.felles.integrasjon.kafka;

import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;

import no.nav.foreldrepenger.konfig.Environment;

public class KafkaProperties {

    private static final Environment ENV = Environment.current();
    private static final boolean IS_DEPLOYMENT = ENV.isProd() || ENV.isDev();
    private static final String APPLICATION_NAME = ENV.getNaisAppName();

    public static Properties forProducer(String bootstrapServers,
                                         String trustStorePath,
                                         String keyStoreLocation,
                                         String credStorePassword) {

        final Properties props = new Properties();
        props.put(CommonClientConfigs.CLIENT_ID_CONFIG, generateClientId(APPLICATION_NAME));
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        putSecurity(props, trustStorePath, keyStoreLocation, credStorePassword);
        if (IS_DEPLOYMENT) {
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, credStorePassword); // Kun producer
        }

        // Serde
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return props;
    }

    public static Properties forStreamsString(String applicationId,
                                              String bootstrapServers,
                                              String trustStorePath,
                                              String keyStoreLocation,
                                              String credStorePassword) {

        return forGenericValue(applicationId,  Serdes.String(), bootstrapServers,
            trustStorePath, keyStoreLocation, credStorePassword);
    }

    public static <T> Properties forGenericValue(String applicationId,
                                                 Serde<T> valueSerde,
                                                 String bootstrapServers,
                                                 String trustStorePath,
                                                 String keyStoreLocation,
                                                 String credStorePassword) {
        final Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, generateClientId(APPLICATION_NAME));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        putSecurity(props, trustStorePath, keyStoreLocation, credStorePassword);

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, valueSerde.getClass());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler.class);

        // Polling
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "200");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "60000");

        return props;
    }

    private static String generateClientId(String source) {
        return source + "-" + UUID.randomUUID();
    }

    private static void putSecurity(Properties props,
                                    String trustStorePath,
                                    String keyStoreLocation,
                                    String credStorePassword) {
        if (IS_DEPLOYMENT) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks");
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStorePath);
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, credStorePassword);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStoreLocation);
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, credStorePassword);
        } else {
            props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
            props.setProperty(SaslConfigs.SASL_MECHANISM, "PLAIN");
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, "vtp", "vtp");
            props.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasCfg);
        }
    }

}
