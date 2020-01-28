package no.nav.foreldrepenger.risk.integrasjon.organisasjon;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.OrganisasjonV5;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class OrganisasjonConsumerProducer {
    private OrganisasjonConsumerConfig consumerConfig;

    @Inject
    public void setConfig(OrganisasjonConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public OrganisasjonConsumer organisasjonConsumer() {
        OrganisasjonV5 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new OrganisasjonConsumerImpl(port);
    }

    public OrganisasjonSelftestConsumer organisasjonSelftestConsumer() {
        OrganisasjonV5 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new OrganisasjonSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    OrganisasjonV5 wrapWithSts(OrganisasjonV5 port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

}
