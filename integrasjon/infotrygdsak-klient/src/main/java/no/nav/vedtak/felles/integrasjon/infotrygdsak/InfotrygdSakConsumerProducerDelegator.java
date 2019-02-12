package no.nav.vedtak.felles.integrasjon.infotrygdsak;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class InfotrygdSakConsumerProducerDelegator {
    private InfotrygdSakConsumerProducer producer;

    @Inject
    public InfotrygdSakConsumerProducerDelegator(InfotrygdSakConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public InfotrygdSakConsumer journalConsumerForEndUser() {
        return producer.infotrygdSakConsumer();
    }

    @Produces
    public InfotrygdSakSelftestConsumer infotrygdSakSelftestConsumerForSystemUser() {
        return producer.infotrygdSakSelftestConsumer();
    }
}