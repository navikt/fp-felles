package no.nav.vedtak.felles.integrasjon.behandleinngaaendejournal;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class BehandleInngaaendeJournalConsumerProducer {

    private BehandleInngaaendeJournalConsumerConfig consumerConfig;


    @Inject
    public void setConfig(BehandleInngaaendeJournalConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public BehandleInngaaendeJournalConsumer behandleInngaaendeJournalConsumer() {
        BehandleInngaaendeJournalV1 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new BehandleInngaaendeJournalConsumerImpl(port);
    }

    public BehandleInngaaendeJournalSelftestConsumer behandleInngaaendeJournalSelftestConsumer() {
        BehandleInngaaendeJournalV1 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new BehandleInngaaendeJournalSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    BehandleInngaaendeJournalV1 wrapWithSts(BehandleInngaaendeJournalV1 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}
