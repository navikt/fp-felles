package no.vedtak.felles.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class AksjonspunktMeldingProducer extends GenerellMeldingProducer implements MeldingProducer {

    public AksjonspunktMeldingProducer() {
        // for CDI proxy
    }

    @Inject
    public AksjonspunktMeldingProducer(@KonfigVerdi("kafka.aksjonspunkthendelse.topic") String topic,
                                       @KonfigVerdi("bootstrap.servers") String bootstrapServers,
                                       @KonfigVerdi("kafka.aksjonspunkthendelse.schema.registry.url") String schemaRegistryUrl,
                                       @KonfigVerdi("kafka.aksjonspunkthendelse.client.id") String clientId,
                                       @KonfigVerdi("systembruker.username") String username,
                                       @KonfigVerdi("systembruker.password") String password) {
        super(topic, bootstrapServers, schemaRegistryUrl, clientId, username, password);
    }
}
