package no.nav.vedtak.felles.integrasjon.arbeidsforhold;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class ArbeidsforholdConsumerProducerDelegator {
    private ArbeidsforholdConsumerProducer producer;

    @Inject
    public ArbeidsforholdConsumerProducerDelegator(ArbeidsforholdConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public ArbeidsforholdConsumer arbeidsforholdConsumerForEndUser() {
        return producer.arbeidsforholdConsumer();
    }

    @Produces
    public ArbeidsforholdSelftestConsumer arbeidsforholdSelftestConsumerForSystemUser() {
        return producer.arbeidsforholdSelftestConsumer();
    }
}
