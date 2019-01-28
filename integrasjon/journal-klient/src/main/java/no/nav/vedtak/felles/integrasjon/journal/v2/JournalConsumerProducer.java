package no.nav.vedtak.felles.integrasjon.journal.v2;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.journal.v2.binding.JournalV2;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class JournalConsumerProducer {
    private JournalConsumerConfig consumerConfig;

    @Inject
    public void setConfig(JournalConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public JournalConsumer journalConsumer() {
        JournalV2 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new JournalConsumerImpl(port);
    }

    public JournalSelftestConsumer journalSelftestConsumer() {
        JournalV2 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new JournalSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    JournalV2 wrapWithSts(JournalV2 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}
