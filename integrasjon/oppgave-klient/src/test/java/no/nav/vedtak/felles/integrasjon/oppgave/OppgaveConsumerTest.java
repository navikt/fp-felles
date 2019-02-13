package no.nav.vedtak.felles.integrasjon.oppgave;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.tjeneste.virksomhet.oppgave.v3.binding.OppgaveV3;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeRequest;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.HentOppgaveRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class OppgaveConsumerTest {

    private OppgaveConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private OppgaveV3 mockWebservice = mock(OppgaveV3.class);

    @Before
    public void setUp() {
        consumer = new OppgaveConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_finnOppgaveListe() throws Exception {
        when(mockWebservice.finnOppgaveListe(any(FinnOppgaveListeRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        FinnOppgaveListeRequestMal mal = mock(FinnOppgaveListeRequestMal.class);
        FinnOppgaveListeSokMal sok = FinnOppgaveListeSokMal.builder()
                .medAnsvarligEnhetId("124")
                .medBrukerId("123")
                .medSakId("123")
                .build();

        when(mal.getSok()).thenReturn(sok);
        when(mal.getFilter()).thenReturn(null);
        when(mal.getSorteringKode()).thenReturn(null);
        when(mal.getIkkeTidligereFordeltTil()).thenReturn(null);

        consumer.finnOppgaveListe(mal);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentOppgave() throws Exception {
        when(mockWebservice.hentOppgave(any(HentOppgaveRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        HentOppgaveRequest request = new HentOppgaveRequest();
        request.setOppgaveId("123123123");

        consumer.hentOppgave(request);
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
