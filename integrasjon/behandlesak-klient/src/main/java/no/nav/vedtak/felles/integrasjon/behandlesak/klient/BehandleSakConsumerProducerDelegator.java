package no.nav.vedtak.felles.integrasjon.behandlesak.klient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class BehandleSakConsumerProducerDelegator {
    private BehandleSakConsumerProducer producer;

    @Inject
    public BehandleSakConsumerProducerDelegator(BehandleSakConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public BehandleSakConsumer behandleSakConsumerForEndUser() {
        return producer.behandleSakConsumer();
    }

    @Produces
    public BehandleSakSelftestConsumer behandleSakSelftestConsumerForSystemUser() {
        return producer.behandleSakSelftestConsumer();
    }
}
