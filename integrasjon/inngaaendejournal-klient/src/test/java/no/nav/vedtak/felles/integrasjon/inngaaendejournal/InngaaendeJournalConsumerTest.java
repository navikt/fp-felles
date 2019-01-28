package no.nav.vedtak.felles.integrasjon.inngaaendejournal;

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

import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.InngaaendeJournalV1;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class InngaaendeJournalConsumerTest {

    private InngaaendeJournalConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private InngaaendeJournalV1 mockWebservice = mock(InngaaendeJournalV1.class);

    @Before
    public void setUp() {
        consumer = new InngaaendeJournalConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentJournalpost() throws Exception {
        when(mockWebservice.hentJournalpost(any(HentJournalpostRequest.class))).thenThrow(opprettSOAPFaultException("fault"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.hentJournalpost(mock(HentJournalpostRequest.class));
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_utledJournalfoeringsbehov() throws Exception {
        when(mockWebservice.utledJournalfoeringsbehov(any(UtledJournalfoeringsbehovRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.utledJournalfoeringsbehov(mock(UtledJournalfoeringsbehovRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
