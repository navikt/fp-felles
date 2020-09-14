package no.nav.vedtak.felles.integrasjon.person;

import static no.nav.vedtak.sts.client.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.vedtak.sts.client.StsClientType;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class PersonConsumerProducer {
    private PersonConsumerConfig consumerConfig;

    @Inject
    public void setConfig(PersonConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public PersonConsumer personConsumer() {
        PersonV3 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new PersonConsumerImpl(port);
    }

    public PersonSelftestConsumer personSelftestConsumer() {
        PersonV3 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new PersonSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    PersonV3 wrapWithSts(PersonV3 port, StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

}
