package no.vedtak.felles.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.Producer;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class DokumentMeldingProducer extends GenerellMeldingProducer implements MeldingProducer {

    private Producer<String, String> producer;
    private String topic;

    public DokumentMeldingProducer() {
        // for CDI proxy
    }

    @Inject
    public DokumentMeldingProducer(@KonfigVerdi("kafka.dokumenthendelse.topic") String topic,
                                   @KonfigVerdi("bootstrap.servers") String bootstrapServers,
                                   @KonfigVerdi("kafka.dokumenthendelse.schema.registry.url") String schemaRegistryUrl,
                                   @KonfigVerdi("kafka.dokumenthendelse.client.id") String clientId,
                                   @KonfigVerdi("systembruker.username") String username,
                                   @KonfigVerdi("systembruker.password") String password) {
        super(topic, bootstrapServers, schemaRegistryUrl, clientId, username, password);
    }

}
