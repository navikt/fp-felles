package no.nav.vedtak.felles.integrasjon.sak;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class SakConsumerProducerDelegator {
    private SakConsumerProducer producer;

    @Inject
    public SakConsumerProducerDelegator(SakConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public SakConsumer sakConsumerForEndUser() {
        return producer.sakConsumer();
    }

    @Produces
    public SakSelftestConsumer sakSelftestConsumerForSystemUser() {
        return producer.sakSelftestConsumer();
    }
}
