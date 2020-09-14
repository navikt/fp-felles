package no.nav.vedtak.felles.integrasjon.journal.v3;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.journal.v3.JournalV3;
import no.nav.vedtak.sts.client.StsClientType;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class JournalConsumerProducer {
    private JournalConsumerConfig consumerConfig;

    @Inject
    public void setConfig(JournalConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public JournalConsumer journalConsumer() {
        JournalV3 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new JournalConsumerImpl(port);
    }

    public JournalSelftestConsumer journalSelftestConsumer() {
        JournalV3 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new JournalSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    JournalV3 wrapWithSts(JournalV3 port, StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

}
