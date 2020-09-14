package no.nav.vedtak.felles.integrasjon.medl;

import static no.nav.vedtak.sts.client.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.medlemskap.v2.MedlemskapV2;
import no.nav.vedtak.sts.client.StsClientType;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class MedlemConsumerProducer {

    private MedlemConsumerConfig consumerConfig;

    @Inject
    public void setConfig(MedlemConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public MedlemConsumer medlemConsumer() {
        MedlemskapV2 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new MedlemConsumerImpl(port);
    }

    public MedlemSelftestConsumer medlemSelftestConsumer() {
        MedlemskapV2 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new MedlemSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    MedlemskapV2 wrapWithSts(MedlemskapV2 port, StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

}
