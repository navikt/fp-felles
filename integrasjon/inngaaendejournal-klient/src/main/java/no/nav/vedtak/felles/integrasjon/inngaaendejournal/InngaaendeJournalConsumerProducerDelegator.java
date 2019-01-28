package no.nav.vedtak.felles.integrasjon.inngaaendejournal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class InngaaendeJournalConsumerProducerDelegator {

    private InngaaendeJournalConsumerProducer producer;

    @Inject
    public InngaaendeJournalConsumerProducerDelegator(InngaaendeJournalConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public InngaaendeJournalConsumer inngaaendeJournalConsumerForEndUser() {
        return producer.inngaaendeJournalConsumer();
    }

    @Produces
    public InngaaendeJournalSelftestConsumer inngaaendeJournalSelftestConsumerForSystemUser() {
        return producer.inngaaendeJournalSelftestConsumer();
    }
}
