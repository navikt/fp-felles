package no.nav.vedtak.felles.integrasjon.inntekt;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.inntekt.v3.binding.InntektV3;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class InntektConsumerProducer {
    private InntektConsumerConfig consumerConfig;

    @Inject
    public void setConfig(InntektConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public InntektConsumer inntektConsumer() {
        InntektV3 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new InntektConsumerImpl(port);
    }

    public InntektSelftestConsumer inntektSelftestConsumer() {
        InntektV3 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new InntektSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    InntektV3 wrapWithSts(InntektV3 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}
