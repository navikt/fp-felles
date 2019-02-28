package no.vedtak.felles.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class DokumentMeldingConsumer extends GenerellMeldingConsumer implements MeldingConsumer {

    public DokumentMeldingConsumer() {

    }

    @Inject
    public DokumentMeldingConsumer(@KonfigVerdi("kafka.dokumenthendelse.topic") String topic,
                                   @KonfigVerdi("bootstrap.servers") String bootstrapServers,
                                   @KonfigVerdi("kafka.dokumenthendelse.schema.registry.url") String schemaRegistryUrl,
                                   @KonfigVerdi("kafka.dokumenthendelse.group.id") String groupId,
                                   @KonfigVerdi("systembruker.username") String username,
                                   @KonfigVerdi("systembruker.password") String password) {

        super(topic, bootstrapServers, schemaRegistryUrl, groupId, username, password);
    }
}
