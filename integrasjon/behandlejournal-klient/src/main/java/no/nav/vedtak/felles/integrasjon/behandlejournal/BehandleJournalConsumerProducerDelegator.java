package no.nav.vedtak.felles.integrasjon.behandlejournal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class BehandleJournalConsumerProducerDelegator {

    private BehandleJournalConsumerProducer producer;

    @Inject
    public BehandleJournalConsumerProducerDelegator(BehandleJournalConsumerProducer producer){
        this.producer = producer;
    }

    @Produces
    public BehandleJournalConsumer journalConsumerForEndUser() {
        return producer.behandleJournalConsumer();
    }

    @Produces
    public BehandleJournalSelftestConsumer journalSelftestConsumerForSystemUser() {
        return producer.behandleJournalSelftestConsumer();
    }
}
