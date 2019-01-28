package no.nav.vedtak.felles.integrasjon.behandleoppgave;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class BehandleoppgaveConsumerProducer {
    private BehandleoppgaveConsumerConfig consumerConfig;

    @Inject
    public void setConfig(BehandleoppgaveConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public BehandleoppgaveConsumer behandleoppgaveConsumer() {
        BehandleOppgaveV1 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new BehandleoppgaveConsumerImpl(port);
    }

    public BehandleoppgaveSelftestConsumer behandleoppgaveSelftestConsumer() {
    	BehandleOppgaveV1 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new BehandleoppgaveSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    BehandleOppgaveV1 wrapWithSts(BehandleOppgaveV1 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

}
