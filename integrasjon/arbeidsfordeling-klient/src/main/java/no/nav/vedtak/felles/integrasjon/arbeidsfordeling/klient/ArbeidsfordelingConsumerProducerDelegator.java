package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class ArbeidsfordelingConsumerProducerDelegator {
    private ArbeidsfordelingConsumerProducer producer;

    @Inject
    public ArbeidsfordelingConsumerProducerDelegator(ArbeidsfordelingConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public ArbeidsfordelingConsumer arbeidsfordelingConsumerForEndUser() {
        return producer.arbeidsfordelingConsumer();
    }

    @Produces
    public ArbeidsfordelingSelftestConsumer arbeidsfordelingSelftestConsumerForSystemUser() {
        return producer.arbeidsfordelingSelftestConsumer();
    }
}
