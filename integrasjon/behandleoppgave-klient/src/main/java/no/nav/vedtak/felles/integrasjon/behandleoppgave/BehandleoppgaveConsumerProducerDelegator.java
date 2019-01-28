package no.nav.vedtak.felles.integrasjon.behandleoppgave;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class BehandleoppgaveConsumerProducerDelegator {
    private BehandleoppgaveConsumerProducer producer;

    @Inject
    public BehandleoppgaveConsumerProducerDelegator(BehandleoppgaveConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public BehandleoppgaveConsumer behandleoppgaveConsumerForEndUser() {
        return producer.behandleoppgaveConsumer();
    }

    @Produces
    public BehandleoppgaveSelftestConsumer behandleoppgaveSelftestConsumerForSystemUser() {
        return producer.behandleoppgaveSelftestConsumer();
    }
}
