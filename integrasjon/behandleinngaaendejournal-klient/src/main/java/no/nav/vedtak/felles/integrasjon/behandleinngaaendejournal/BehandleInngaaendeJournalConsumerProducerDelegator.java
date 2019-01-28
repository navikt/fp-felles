package no.nav.vedtak.felles.integrasjon.behandleinngaaendejournal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class BehandleInngaaendeJournalConsumerProducerDelegator {

    private BehandleInngaaendeJournalConsumerProducer producer;

    @Inject
    public BehandleInngaaendeJournalConsumerProducerDelegator(BehandleInngaaendeJournalConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public BehandleInngaaendeJournalConsumer inngaaendeJournalConsumerForEndUser() {
        return producer.behandleInngaaendeJournalConsumer();
    }

    @Produces
    public BehandleInngaaendeJournalSelftestConsumer inngaaendeJournalSelftestConsumerForSystemUser() {
        return producer.behandleInngaaendeJournalSelftestConsumer();
    }
}
