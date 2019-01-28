package no.nav.vedtak.felles.integrasjon.arbeidsforhold;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class ArbeidsforholdConsumerProducer {
    private ArbeidsforholdConsumerConfig consumerConfig;

    @Inject
    public void setConfig(ArbeidsforholdConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public ArbeidsforholdConsumer arbeidsforholdConsumer() {
        ArbeidsforholdV3 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new ArbeidsforholdConsumerImpl(port);
    }

    public ArbeidsforholdSelftestConsumer arbeidsforholdSelftestConsumer() {
        ArbeidsforholdV3 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new ArbeidsforholdSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    ArbeidsforholdV3 wrapWithSts(ArbeidsforholdV3 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}
