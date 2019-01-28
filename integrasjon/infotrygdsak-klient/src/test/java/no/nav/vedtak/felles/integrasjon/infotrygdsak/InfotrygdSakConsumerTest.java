package no.nav.vedtak.felles.integrasjon.infotrygdsak;

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

import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.InfotrygdSakV1;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class InfotrygdSakConsumerTest {

    private InfotrygdSakConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private InfotrygdSakV1 mockWebservice = mock(InfotrygdSakV1.class);

    @Before
    public void setUp() {
        consumer = new InfotrygdSakConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault() throws Exception {
        when(mockWebservice.finnSakListe(any(FinnSakListeRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.finnSakListe(mock(FinnSakListeRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
