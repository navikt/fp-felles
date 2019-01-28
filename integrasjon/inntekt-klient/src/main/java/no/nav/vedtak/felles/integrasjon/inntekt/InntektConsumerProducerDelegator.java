package no.nav.vedtak.felles.integrasjon.inntekt;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class InntektConsumerProducerDelegator {
    private InntektConsumerProducer producer;

    @Inject
    public InntektConsumerProducerDelegator(InntektConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public InntektConsumer inntektConsumerForEndUser() {
        return producer.inntektConsumer();
    }

    @Produces
    public InntektSelftestConsumer inntektSelftestConsumerForSystemUser() {
        return producer.inntektSelftestConsumer();
    }
}
