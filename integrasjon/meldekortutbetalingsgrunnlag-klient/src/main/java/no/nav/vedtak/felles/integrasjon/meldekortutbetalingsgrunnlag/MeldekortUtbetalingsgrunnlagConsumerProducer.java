package no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.MeldekortUtbetalingsgrunnlagV1;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class MeldekortUtbetalingsgrunnlagConsumerProducer {
    private MeldekortUtbetalingsgrunnlagConsumerConfig consumerConfig;

    @Inject
    public void setConfig(MeldekortUtbetalingsgrunnlagConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public MeldekortUtbetalingsgrunnlagConsumer meldekortUtbetalingsgrunnlagConsumer() {
        MeldekortUtbetalingsgrunnlagV1 port = StsConfigurationUtil.wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new MeldekortUtbetalingsgrunnlagConsumerImpl(port);
    }

    public MeldekortUtbetalingsgrunnlagSelftestConsumer meldekortUtbetalingsgrunnlagSelftestConsumer() {
        MeldekortUtbetalingsgrunnlagV1 port = StsConfigurationUtil.wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new MeldekortUtbetalingsgrunnlagSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }
}