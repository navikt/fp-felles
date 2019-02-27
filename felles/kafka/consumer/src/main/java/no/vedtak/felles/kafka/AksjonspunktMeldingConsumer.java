package no.vedtak.felles.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class AksjonspunktMeldingConsumer extends GenerellMeldingConsumer implements MeldingConsumer {

    public AksjonspunktMeldingConsumer() {

    }

    @Inject
    public AksjonspunktMeldingConsumer(@KonfigVerdi("kafka.aksjonspunkthendelse.topic") String topic,
                                       @KonfigVerdi("bootstrap.servers") String bootstrapServers,
                                       @KonfigVerdi("kafka.aksjonspunkthendelse.schema.registry.url") String schemaRegistryUrl,
                                       @KonfigVerdi("kafka.aksjonspunkthendelse.group.id") String groupId,
                                       @KonfigVerdi("systembruker.username") String username,
                                       @KonfigVerdi("systembruker.password") String password) {
        super(topic, bootstrapServers, schemaRegistryUrl, groupId, username, password);
    }
}
