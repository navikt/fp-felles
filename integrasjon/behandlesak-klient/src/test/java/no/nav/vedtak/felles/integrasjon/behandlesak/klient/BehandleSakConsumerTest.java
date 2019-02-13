package no.nav.vedtak.felles.integrasjon.behandlesak.klient;

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

import no.nav.tjeneste.virksomhet.behandlesak.v2.BehandleSakV2;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSOpprettSakRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class BehandleSakConsumerTest {

    private BehandleSakConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    BehandleSakV2 mockService = mock(BehandleSakV2.class);

    @Before
    public void setUp() {
        consumer = new BehandleSakConsumerImpl(mockService);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault() throws Exception {
        when(mockService.opprettSak(any(WSOpprettSakRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.opprettSak(mock(WSOpprettSakRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
