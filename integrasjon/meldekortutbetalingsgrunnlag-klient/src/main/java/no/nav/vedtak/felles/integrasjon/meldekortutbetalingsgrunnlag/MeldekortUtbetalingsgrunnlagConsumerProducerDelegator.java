package no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class MeldekortUtbetalingsgrunnlagConsumerProducerDelegator {
    private MeldekortUtbetalingsgrunnlagConsumerProducer producer;

    @Inject
    public MeldekortUtbetalingsgrunnlagConsumerProducerDelegator(MeldekortUtbetalingsgrunnlagConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public MeldekortUtbetalingsgrunnlagConsumer meldekortUtbetalingsgrunnlagConsumerForEndUser() {
        return producer.meldekortUtbetalingsgrunnlagConsumer();
    }

    @Produces
    public MeldekortUtbetalingsgrunnlagSelftestConsumer meldekortUtbetalingsgrunnlagSelftestConsumerForSystemUser() {
        return producer.meldekortUtbetalingsgrunnlagSelftestConsumer();
    }
}