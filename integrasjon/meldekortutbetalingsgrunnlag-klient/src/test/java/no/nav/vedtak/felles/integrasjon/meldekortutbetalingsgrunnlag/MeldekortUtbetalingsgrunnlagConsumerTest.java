package no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag;

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

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.MeldekortUtbetalingsgrunnlagV1;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class MeldekortUtbetalingsgrunnlagConsumerTest {

    private MeldekortUtbetalingsgrunnlagConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private MeldekortUtbetalingsgrunnlagV1 mockWebservice = mock(MeldekortUtbetalingsgrunnlagV1.class);

    @Before
    public void setUp() {
        consumer = new MeldekortUtbetalingsgrunnlagConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault() throws Exception {
        when(mockWebservice.finnMeldekortUtbetalingsgrunnlagListe(any(FinnMeldekortUtbetalingsgrunnlagListeRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.finnMeldekortUtbetalingsgrunnlagListe(mock(FinnMeldekortUtbetalingsgrunnlagListeRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
