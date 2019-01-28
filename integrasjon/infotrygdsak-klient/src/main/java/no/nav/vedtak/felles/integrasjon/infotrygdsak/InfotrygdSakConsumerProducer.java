package no.nav.vedtak.felles.integrasjon.infotrygdsak;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.InfotrygdSakV1;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class InfotrygdSakConsumerProducer {
    private InfotrygdSakConsumerConfig consumerConfig;

    @Inject
    public void setConfig(InfotrygdSakConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public InfotrygdSakConsumer infotrygdSakConsumer() {
        InfotrygdSakV1 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new InfotrygdSakConsumerImpl(port);
    }

    public InfotrygdSakSelftestConsumer infotrygdSakSelftestConsumer() {
        InfotrygdSakV1 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new InfotrygdSakSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    private InfotrygdSakV1 wrapWithSts(InfotrygdSakV1 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}