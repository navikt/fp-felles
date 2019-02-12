package no.nav.vedtak.felles.integrasjon.behandlejournal;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class BehandleJournalConsumerProducer {

    private BehandleJournalConsumerConfig consumerConfig;

    @Inject
    public void setConfig(BehandleJournalConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public BehandleJournalConsumer behandleJournalConsumer() {
        BehandleJournalV3 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new BehandleJournalConsumerImpl(port);
    }

    public BehandleJournalSelftestConsumer behandleJournalSelftestConsumer() {
        BehandleJournalV3 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new BehandleJournalSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    BehandleJournalV3 wrapWithSts(BehandleJournalV3 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}
