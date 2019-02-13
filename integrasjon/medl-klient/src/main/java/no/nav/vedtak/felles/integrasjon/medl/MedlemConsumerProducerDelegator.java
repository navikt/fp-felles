package no.nav.vedtak.felles.integrasjon.medl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class MedlemConsumerProducerDelegator {

    private MedlemConsumerProducer producer;

    @Inject
    public MedlemConsumerProducerDelegator(MedlemConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public MedlemConsumer medlemConsumerForEndUser() {
        return producer.medlemConsumer();
    }

    @Produces
    public MedlemSelftestConsumer medlemSelftestConsumerForSystemUser() {
        return producer.medlemSelftestConsumer();
    }
}
