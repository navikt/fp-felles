package no.nav.vedtak.felles.integrasjon.oppgave;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class OppgaveConsumerProducerDelegator {
    private OppgaveConsumerProducer producer;

    @Inject
    public OppgaveConsumerProducerDelegator(OppgaveConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public OppgaveConsumer arbeidsfordelingConsumerForEndUser() {
        return producer.oppgaveConsumer();
    }

    @Produces
    public OppgaveSelftestConsumer arbeidsfordelingSelftestConsumerForSystemUser() {
        return producer.oppgaveSelftestConsumer();
    }
}
