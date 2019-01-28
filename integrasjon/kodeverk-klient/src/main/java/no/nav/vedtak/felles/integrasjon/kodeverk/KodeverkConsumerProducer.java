package no.nav.vedtak.felles.integrasjon.kodeverk;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class KodeverkConsumerProducer {
    private KodeverkConsumerConfig consumerConfig;

    @Inject
    public void setConfig(KodeverkConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public KodeverkConsumer kodeverkConsumer() {
        KodeverkPortType port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new KodeverkConsumerImpl(port);
    }

    public KodeverkSelftestConsumer kodeverkSelftestConsumer() {
        KodeverkPortType port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new KodeverkSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    KodeverkPortType wrapWithSts(KodeverkPortType port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}
