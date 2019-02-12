package no.nav.vedtak.felles.integrasjon.inntekt;

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

import no.nav.tjeneste.virksomhet.inntekt.v3.binding.InntektV3;
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class InntektConsumerTest {

    private InntektConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private InntektV3 mockWebservice = mock(InntektV3.class);

    @Before
    public void setUp() {
        consumer = new InntektConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault() throws Exception {
        when(mockWebservice.hentInntektListeBolk(any(HentInntektListeBolkRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.hentInntektListeBolk(mock(HentInntektListeBolkRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
