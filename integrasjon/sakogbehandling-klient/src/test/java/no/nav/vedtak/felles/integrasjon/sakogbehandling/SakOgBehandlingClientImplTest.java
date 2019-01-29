package no.nav.vedtak.felles.integrasjon.sakogbehandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Before;
import org.junit.Test;

import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Aktoer;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Applikasjoner;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Avslutningsstatuser;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingAvsluttet;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingOpprettet;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Behandlingstemaer;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Behandlingstyper;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Sakstemaer;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.log.mdc.MDCOperations;

public class SakOgBehandlingClientImplTest {

    private SakOgBehandlingClientImpl client; // objektet vi tester
    private SakOgBehandlingJmsKonfig jmsKonfig = new SakOgBehandlingJmsKonfig();

    private Queue mockQueue;
    private JMSProducer mockJMSProducer;
    private JMSContext mockJMSContext;

    @Before
    public void setup() throws JMSException {
        mockQueue = mock(Queue.class);
        mockJMSProducer = mock(JMSProducer.class);
        mockJMSContext = mock(JMSContext.class);
        when(mockJMSContext.createProducer()).thenReturn(mockJMSProducer);

        client = new SakOgBehandlingClientImpl(jmsKonfig) {
            @Override
            protected JMSContext createContext() {
                return mockJMSContext;
            }
        };
        Whitebox.setInternalState(client, "queue", mockQueue);
    }

    @Test
    public void test_mapTilBehandlingOpprettetXml() throws Exception {
        BehandlingOpprettet behandlingOpprettet = lagBehandlingOpprettet();

        client.mapTilBehandlingOpprettetXml(behandlingOpprettet);
    }

    @Test
    public void test_mapTilBehandlingAvsluttetXml() throws Exception {
        BehandlingAvsluttet behandlingAvsluttet = lagBehandlingAvsluttet();

        client.mapTilBehandlingAvsluttetXml(behandlingAvsluttet);
    }

    @Test
    public void test_sendBehandlingOpprettet() throws DatatypeConfigurationException {

        BehandlingOpprettet behandlingOpprettet = lagBehandlingOpprettet();
        final String callId;
        try {
            callId = MDCOperations.generateCallId();
            MDCOperations.putCallId(callId);

            client.sendBehandlingOpprettet(behandlingOpprettet);
        } finally {
            MDCOperations.removeCallId();
        }

        verify(mockJMSProducer).setProperty(eq("callId"), same(callId));
        verify(mockJMSProducer).send(same(mockQueue), anyString());
    }

    @Test
    public void test_sendBehandlingAvsluttet() {

        BehandlingAvsluttet behandlingAvsluttet = lagBehandlingAvsluttet();
        final String callId;
        try {
            callId = MDCOperations.generateCallId();
            MDCOperations.putCallId(callId);

            client.sendBehandlingAvsluttet(behandlingAvsluttet);
        } finally {
            MDCOperations.removeCallId();
        }

        verify(mockJMSProducer).setProperty(eq("callId"), same(callId));
        verify(mockJMSProducer).send(same(mockQueue), anyString());
    }

    @Test
    public void test_getConnectionEndpoint() {
        jmsKonfig.setQueueName("myQueue");
        jmsKonfig.setQueueManagerName("myMgr");
        jmsKonfig.setQueueManagerHostname("myHost");
        jmsKonfig.setQueueManagerPort(8009);

        String endpt = client.getConnectionEndpoint();

        assertThat(endpt).isNotNull();
    }

    private BehandlingAvsluttet lagBehandlingAvsluttet() {
        BehandlingAvsluttet behandlingAvsluttet = new BehandlingAvsluttet();

        behandlingAvsluttet.setBehandlingsID("APPLIKASJON_ID_VL_INTTEST_1");

        behandlingAvsluttet.setHendelsesId("callid_123456");

        Applikasjoner applikasjoner = new Applikasjoner();
        applikasjoner.setValue("APPLIKASJON_ID");
        behandlingAvsluttet.setHendelsesprodusentREF(applikasjoner);

        Avslutningsstatuser avslutningsstatuser = new Avslutningsstatuser();
        avslutningsstatuser.setValue("OK");
        behandlingAvsluttet.setAvslutningsstatus(avslutningsstatuser);

        behandlingAvsluttet.setAnsvarligEnhetREF("4833");

        Behandlingstyper behandlingstype = new Behandlingstyper();
        behandlingstype.setValue("behandlingstype");
        behandlingAvsluttet.setBehandlingstype(behandlingstype);

        Sakstemaer sakstema = new Sakstemaer();
        sakstema.setValue("sakstema");
        behandlingAvsluttet.setSakstema(sakstema);

        Aktoer aktoer = new Aktoer();
        aktoer.setAktoerId("123");
        behandlingAvsluttet.getAktoerREF().add(aktoer);

        return behandlingAvsluttet;
    }

    private BehandlingOpprettet lagBehandlingOpprettet() throws DatatypeConfigurationException {
        BehandlingOpprettet behandlingOpprettet = new BehandlingOpprettet();

        Behandlingstemaer behandlingstema = new Behandlingstemaer();
        behandlingstema.setValue("behandlingstema");
        behandlingOpprettet.setBehandlingstema(behandlingstema);

        behandlingOpprettet.setHendelsesId("hendelsesId");

        Applikasjoner applikasjoner = new Applikasjoner();
        applikasjoner.setValue("applikasjoner");
        behandlingOpprettet.setHendelsesprodusentREF(applikasjoner);

        behandlingOpprettet.setHendelsesTidspunkt(DateUtil.convertToXMLGregorianCalendar(LocalDate.now()));

        behandlingOpprettet.setBehandlingsID("behandlingsId");

        Behandlingstyper behandlingstype = new Behandlingstyper();
        behandlingstype.setValue("behandlingstype");
        behandlingOpprettet.setBehandlingstype(behandlingstype);

        Sakstemaer sakstema = new Sakstemaer();
        sakstema.setValue("sakstema");

        Aktoer aktoer = new Aktoer();
        aktoer.setAktoerId("123");
        behandlingOpprettet.getAktoerREF().add(aktoer);

        return behandlingOpprettet;
    }
}
