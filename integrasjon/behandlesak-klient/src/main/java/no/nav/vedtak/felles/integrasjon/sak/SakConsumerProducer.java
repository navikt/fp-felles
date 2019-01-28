package no.nav.vedtak.felles.integrasjon.sak;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.sak.v1.binding.SakV1;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class SakConsumerProducer {
    private SakConsumerConfig consumerConfig;

    @Inject
    public void setConfig(SakConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public SakConsumer sakConsumer() {
        SakV1 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new SakConsumerImpl(port);
    }

    public SakSelftestConsumer sakSelftestConsumer() {
        SakV1 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new SakSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    SakV1 wrapWithSts(SakV1 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

}
