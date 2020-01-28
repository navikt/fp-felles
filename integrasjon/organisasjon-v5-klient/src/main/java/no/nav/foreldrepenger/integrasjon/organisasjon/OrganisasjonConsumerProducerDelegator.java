package no.nav.foreldrepenger.integrasjon.organisasjon;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class OrganisasjonConsumerProducerDelegator {
    private OrganisasjonConsumerProducer producer;

    @Inject
    public OrganisasjonConsumerProducerDelegator(OrganisasjonConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public OrganisasjonConsumer organisasjonConsumerForEndUser() {
        return producer.organisasjonConsumer();
    }

    @Produces
    public OrganisasjonSelftestConsumer oppgavebehandlingSelftestConsumerForSystemUser() {
        return producer.organisasjonSelftestConsumer();
    }
}
