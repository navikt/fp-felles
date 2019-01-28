package no.nav.vedtak.felles.integrasjon.behandleinngaaendejournal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.FerdigstillJournalfoeringRequest;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.OppdaterJournalpostRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class BehandleInngaaendeJournalTest {

    private BehandleInngaaendeJournalConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    BehandleInngaaendeJournalV1 mockWebservice = mock(BehandleInngaaendeJournalV1.class);

    @Before
    public void setUp() {
        consumer = new BehandleInngaaendeJournalConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonExceptionVedFeilIFerdigstilling() throws Exception {
        doThrow(opprettSOAPFaultException("error")).when(mockWebservice).ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.ferdigstillJournalfoering(mock(FerdigstillJournalfoeringRequest.class));
    }

    @Test
    public void skalKasteIntegrasjonExceptionVedFeilIOppdatering() throws Exception {
        doThrow(opprettSOAPFaultException("error")).when(mockWebservice).oppdaterJournalpost(any(OppdaterJournalpostRequest.class));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.oppdaterJournalpost(mock(OppdaterJournalpostRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
