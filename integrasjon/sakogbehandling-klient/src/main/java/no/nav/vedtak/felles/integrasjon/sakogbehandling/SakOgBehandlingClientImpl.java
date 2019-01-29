package no.nav.vedtak.felles.integrasjon.sakogbehandling;

import javax.annotation.Resource;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.xml.bind.JAXBException;

import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingAvsluttet;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingOpprettet;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.ObjectFactory;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.nav.vedtak.felles.integrasjon.jms.ExternalQueueProducer;
import no.nav.vedtak.felles.integrasjon.jms.JmsKonfig;
import no.nav.vedtak.felles.integrasjon.jms.JmsMessage;
import no.nav.vedtak.log.mdc.MDCOperations;

@Dependent
class SakOgBehandlingClientImpl extends ExternalQueueProducer implements SakOgBehandlingClient {

    public SakOgBehandlingClientImpl() {
        // CDI
    }

    @Inject
    public SakOgBehandlingClientImpl(@Named("SakOgBehandling") JmsKonfig konfig) {
        super(konfig);
    }

    @Override
    public void sendBehandlingOpprettet(BehandlingOpprettet behandlingOpprettet) {
        final JmsMessage build = JmsMessage.builder()
                .withMessage(mapTilBehandlingOpprettetXml(behandlingOpprettet))
                .addHeader("callId", MDCOperations.getCallId())
                .build();
        sendTextMessage(build);
    }

    @Override
    public void sendBehandlingAvsluttet(BehandlingAvsluttet behandlingAvsluttet) {
        final JmsMessage build = JmsMessage.builder()
                .withMessage(mapTilBehandlingAvsluttetXml(behandlingAvsluttet))
                .addHeader("callId", MDCOperations.getCallId())
                .build();
        sendTextMessage(build);
    }

    String mapTilBehandlingOpprettetXml(BehandlingOpprettet behandlingOpprettet) {
        try {
            return JaxbHelper.marshalJaxb(BehandlingOpprettet.class, new ObjectFactory().createBehandlingOpprettet(behandlingOpprettet));
        } catch (JAXBException e) {
            throw SakOgBehandlingFeil.FACTORY.feilVedOpprettelseAvMeldingTilSakOgBehandling(e).toException();
        }
    }

    String mapTilBehandlingAvsluttetXml(BehandlingAvsluttet behandlingAvsluttet) {
        try {
            return JaxbHelper.marshalJaxb(BehandlingOpprettet.class, new ObjectFactory().createBehandlingAvsluttet(behandlingAvsluttet));
        } catch (JAXBException e) {
            throw SakOgBehandlingFeil.FACTORY.feilVedAvsluttMeldingStatusTilSakOgBehandling(e).toException();
        }
    }

    @Override
    @Resource(mappedName = SakOgBehandlingJmsKonfig.JNDI_JMS_CONNECTION_FACTORY)
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        super.setConnectionFactory(connectionFactory);
    }

    @Override
    @Resource(mappedName = SakOgBehandlingJmsKonfig.JNDI_QUEUE)
    public void setQueue(Queue queue) {
        super.setQueue(queue);
    }
}
