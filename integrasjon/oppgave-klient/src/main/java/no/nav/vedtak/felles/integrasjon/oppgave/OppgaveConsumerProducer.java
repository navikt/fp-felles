package no.nav.vedtak.felles.integrasjon.oppgave;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.oppgave.v3.binding.OppgaveV3;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class OppgaveConsumerProducer {
    private OppgaveConsumerConfig consumerConfig;

    @Inject
    public void setConfig(OppgaveConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    OppgaveConsumer oppgaveConsumer() {
        OppgaveV3 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new OppgaveConsumerImpl(port);
    }

    OppgaveSelftestConsumer oppgaveSelftestConsumer() {
        OppgaveV3 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new OppgaveSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    private OppgaveV3 wrapWithSts(OppgaveV3 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }
}
