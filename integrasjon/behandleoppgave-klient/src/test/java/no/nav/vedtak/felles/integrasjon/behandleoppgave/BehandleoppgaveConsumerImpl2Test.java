package no.nav.vedtak.felles.integrasjon.behandleoppgave;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSFerdigstillOppgaveRequest;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveRequest;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OpprettOppgaveRequest;

public class BehandleoppgaveConsumerImpl2Test {

    private BehandleoppgaveConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private BehandleOppgaveV1 mockWebservice = mock(BehandleOppgaveV1.class);

    @Before
    public void setUp() throws Exception {
        consumer = new BehandleoppgaveConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_ferdigstill() throws Exception {
        doThrow(opprettSOAPFaultException("feil")).when(mockWebservice).ferdigstillOppgave(any(WSFerdigstillOppgaveRequest.class));

        FerdigstillOppgaveRequestMal req = FerdigstillOppgaveRequestMal.builder()
                .medFerdigstiltAvEnhetId(1)
                .medOppgaveId("1234")
                .build();

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.ferdigstillOppgave(req);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_opprett() throws Exception {
        doThrow(opprettSOAPFaultException("fault")).when(mockWebservice).opprettOppgave(any(WSOpprettOppgaveRequest.class));

        OpprettOppgaveRequest req = OpprettOppgaveRequest.builder()
                .medAktivFra(LocalDate.now())
                .medAktivTil(LocalDate.now().plusDays(4))
                .medAnsvarligEnhetId("1")
                .medBeskrivelse("desc")
                .medBrukerTypeKode(BrukerType.PERSON)
                .medDokumentId("124")
                .medFagomradeKode(FagomradeKode.FOR.getKode())
                .medFnr("123545")
                .medSaksnummer("1245")
                .medPrioritetKode(PrioritetKode.NORM_FOR.name())
                .build();

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.opprettOppgave(req);
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}