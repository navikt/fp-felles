package no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.InfotrygdBeregningsgrunnlagV1;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class InfotrygdBeregningsgrunnlagConsumerProducer {
    private InfotrygdBeregningsgrunnlagConsumerConfig consumerConfig;

    @Inject
    public void setConfig(InfotrygdBeregningsgrunnlagConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public InfotrygdBeregningsgrunnlagConsumer infotrygdBeregningsgrunnlagConsumer() {
        InfotrygdBeregningsgrunnlagV1 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new InfotrygdBeregningsgrunnlagConsumerImpl(port);
    }

    public InfotrygdBeregningsgrunnlagSelftestConsumer infotrygdBeregningsgrunnlagSelftestConsumer() {
        InfotrygdBeregningsgrunnlagV1 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new InfotrygdBeregningsgrunnlagSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    private InfotrygdBeregningsgrunnlagV1 wrapWithSts(InfotrygdBeregningsgrunnlagV1 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}