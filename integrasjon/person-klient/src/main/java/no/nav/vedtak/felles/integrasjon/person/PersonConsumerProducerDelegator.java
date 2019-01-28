package no.nav.vedtak.felles.integrasjon.person;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class PersonConsumerProducerDelegator {
    private PersonConsumerProducer producer;

    @Inject
    public PersonConsumerProducerDelegator(PersonConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public PersonConsumer personConsumerForEndUser() {
        return producer.personConsumer();
    }

    @Produces
    public PersonSelftestConsumer personSelftestConsumerForSystemUser() {
        return producer.personSelftestConsumer();
    }
}
