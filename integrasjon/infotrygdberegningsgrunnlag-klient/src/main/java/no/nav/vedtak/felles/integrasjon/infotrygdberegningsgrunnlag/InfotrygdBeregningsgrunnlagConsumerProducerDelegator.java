package no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class InfotrygdBeregningsgrunnlagConsumerProducerDelegator {
    private InfotrygdBeregningsgrunnlagConsumerProducer producer;

    @Inject
    public InfotrygdBeregningsgrunnlagConsumerProducerDelegator(InfotrygdBeregningsgrunnlagConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public InfotrygdBeregningsgrunnlagConsumer journalConsumerForEndUser() {
        return producer.infotrygdBeregningsgrunnlagConsumer();
    }

    @Produces
    public InfotrygdBeregningsgrunnlagSelftestConsumer infotrygdBeregningsgrunnlagSelftestConsumerForSystemUser() {
        return producer.infotrygdBeregningsgrunnlagSelftestConsumer();
    }
}