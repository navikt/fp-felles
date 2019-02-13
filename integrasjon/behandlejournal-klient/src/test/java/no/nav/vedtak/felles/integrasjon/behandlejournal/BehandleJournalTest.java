package no.nav.vedtak.felles.integrasjon.behandlejournal;

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

import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class BehandleJournalTest {

    private BehandleJournalConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    BehandleJournalV3 mockWebservice = mock(BehandleJournalV3.class);

    @Before
    public void setUp() {
        consumer = new BehandleJournalConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonExceptionVedFeilIJournalfoerNotat() throws Exception{
        doThrow(opprettSOAPFaultException("error")).when(mockWebservice).journalfoerNotat(any(JournalfoerNotatRequest.class));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.journalfoerNotat(mock(JournalfoerNotatRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
