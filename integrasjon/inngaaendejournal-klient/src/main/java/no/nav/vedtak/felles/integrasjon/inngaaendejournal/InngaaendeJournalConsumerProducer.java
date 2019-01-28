package no.nav.vedtak.felles.integrasjon.inngaaendejournal;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.InngaaendeJournalV1;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class InngaaendeJournalConsumerProducer {

    private InngaaendeJournalConsumerConfig consumerConfig;

    @Inject
    public void setConfig(InngaaendeJournalConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public InngaaendeJournalConsumer inngaaendeJournalConsumer() {
        InngaaendeJournalV1 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new InngaaendeJournalConsumerImpl(port);
    }

    public InngaaendeJournalSelftestConsumer inngaaendeJournalSelftestConsumer() {
        InngaaendeJournalV1 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new InngaaendeJournalSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    InngaaendeJournalV1 wrapWithSts(InngaaendeJournalV1 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}
