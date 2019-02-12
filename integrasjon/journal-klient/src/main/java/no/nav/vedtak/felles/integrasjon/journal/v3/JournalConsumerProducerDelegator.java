package no.nav.vedtak.felles.integrasjon.journal.v3;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class JournalConsumerProducerDelegator {
    private JournalConsumerProducer producer;

    @Inject
    public JournalConsumerProducerDelegator(JournalConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public JournalConsumer journalConsumerForEndUser() {
        return producer.journalConsumer();
    }

    @Produces
    public JournalSelftestConsumer journalSelftestConsumerForSystemUser() {
        return producer.journalSelftestConsumer();
    }
}
