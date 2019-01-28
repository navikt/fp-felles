package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class ArbeidsfordelingConsumerProducer {
    private ArbeidsfordelingConsumerConfig consumerConfig;

    @Inject
    public void setConfig(ArbeidsfordelingConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public ArbeidsfordelingConsumer arbeidsfordelingConsumer() {
        ArbeidsfordelingV1 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new ArbeidsfordelingConsumerImpl(port);
    }

    public ArbeidsfordelingSelftestConsumer arbeidsfordelingSelftestConsumer() {
        ArbeidsfordelingV1 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new ArbeidsfordelingSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    ArbeidsfordelingV1 wrapWithSts(ArbeidsfordelingV1 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

}
