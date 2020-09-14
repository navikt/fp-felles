package no.nav.vedtak.felles.integrasjon.aktør.klient;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.vedtak.sts.client.StsClientType;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@ApplicationScoped
public class AktørConsumerProducer {
    private AktørConsumerConfig consumerConfig;

    @Inject
    public void setConfig(AktørConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public AktørConsumer aktørConsumer() {
        AktoerV2 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new AktørConsumerImpl(port);
    }

    public AktørSelftestConsumer aktørSelftestConsumer() {
        AktoerV2 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new AktørSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    AktoerV2 wrapWithSts(AktoerV2 port, StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

}
