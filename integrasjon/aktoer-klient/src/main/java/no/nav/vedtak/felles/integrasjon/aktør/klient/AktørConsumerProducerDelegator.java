package no.nav.vedtak.felles.integrasjon.aktør.klient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
class AktørConsumerProducerDelegator {
    private AktørConsumerProducer producer;

    @Inject
    public AktørConsumerProducerDelegator(AktørConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public AktørConsumer aktørConsumerForEndUser() {
        return producer.aktørConsumer();
    }

    @Produces
    public AktørSelftestConsumer aktørSelftestConsumerForSystemUser() {
        return producer.aktørSelftestConsumer();
    }
}
