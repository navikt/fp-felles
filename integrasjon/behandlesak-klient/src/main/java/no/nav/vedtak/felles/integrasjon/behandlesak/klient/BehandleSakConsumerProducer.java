package no.nav.vedtak.felles.integrasjon.behandlesak.klient;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.behandlesak.v2.BehandleSakV2;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class BehandleSakConsumerProducer {
    private BehandleSakConsumerConfig consumerConfig;

    @Inject
    public void setConfig(BehandleSakConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public BehandleSakConsumer behandleSakConsumer() {
        BehandleSakV2 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new BehandleSakConsumerImpl(port);
    }

    public BehandleSakSelftestConsumer behandleSakSelftestConsumer() {
        BehandleSakV2 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new BehandleSakSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    BehandleSakV2 wrapWithSts(BehandleSakV2 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

}
